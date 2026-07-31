package com.donik1998.cubeclash.core.network.live

import com.donik1998.cubeclash.core.network.dto.CreateSolveRequest
import com.donik1998.cubeclash.core.network.dto.RefreshRequest
import com.donik1998.cubeclash.core.network.dto.UpdatePenaltyRequest
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException

/**
 * End-to-end **behaviour** tests against a running `cubeclash-backend` (global prefix `/v1`).
 * These drive the real Retrofit/OkHttp stack (see [LiveApi]) over real sockets — no MockWebServer.
 *
 * Skipped by default so CI (which has no server) stays green: the class Assume-skips unless the
 * gate is set. To run against a backend on http://localhost:3100:
 *
 * ```sh
 * ./gradlew :core:network:testDebugUnitTest \
 *     --tests '*live.LiveApiTest' \
 *     -Dcubeclash.liveApi=true
 *
 * # against another host (base URL must include /v1):
 * ./gradlew :core:network:testDebugUnitTest --tests '*live.LiveApiTest' \
 *     -Dcubeclash.liveApi=true -Dcubeclash.apiBaseUrl=http://localhost:3100/v1/
 * ```
 *
 * Each test registers a fresh throwaway user in [setUp], so they are order-independent and share
 * no state beyond the rows they create.
 */
class LiveApiTest {

    private lateinit var stack: LiveApi.Stack

    @Before
    fun setUp() {
        assumeTrue(
            "live suite — run with -Dcubeclash.liveApi=true against a running backend (see header)",
            LiveApi.enabled,
        )
        stack = LiveApi.newStack()
    }

    private fun solveBody(
        clientId: String,
        timeMs: Long = 8137,
        penalty: String = "none",
        scramble: String = "R U R' U' F2 L D2 B'",
        solvedAt: String = "2026-07-31T04:00:00.000Z",
    ) = CreateSolveRequest(
        event = "3x3",
        scramble = scramble,
        scrambleSource = "random",
        timeMs = timeMs,
        penalty = penalty,
        solvedAt = solvedAt,
        clientId = clientId,
    )

    @Test
    fun `register then me round-trips a real user`() = runBlocking {
        LiveApi.registerFresh(stack)

        val me = stack.api.me().user
        assertNotNull("GET /me must return a wrapped {user}", me)
        assertTrue("id is non-empty", me!!.id.isNotBlank())
        assertTrue("email contains @", me.email?.contains("@") == true)
        assertEquals("a fresh user starts at elo 1000", 1000, me.elo)
    }

    @Test
    fun `a solve round-trips create then history`() = runBlocking {
        LiveApi.registerFresh(stack)

        val created = stack.api.createSolve(solveBody("live-s1")).solve
        assertNotNull("POST /solves must return a wrapped {solve}", created)
        assertTrue(created!!.id.isNotBlank())

        val history = stack.api.solves(event = "3x3")
        assertTrue(
            "the created solve appears in history",
            history.items.any { it.id == created.id },
        )
    }

    @Test
    fun `idempotent create - same client_id does not duplicate`() = runBlocking {
        LiveApi.registerFresh(stack)

        val a = stack.api.createSolve(solveBody("live-dup", timeMs = 7500)).solve!!
        val b = stack.api.createSolve(solveBody("live-dup", timeMs = 7500)).solve!!
        assertEquals("upsert on (user_id, client_id)", a.id, b.id)

        val rows = stack.api.solves(event = "3x3").items.filter { it.clientId == "live-dup" }
        assertEquals("exactly one row for the client_id", 1, rows.size)
    }

    @Test
    fun `penalty PATCH changes the stored solve`() = runBlocking {
        LiveApi.registerFresh(stack)
        val created = stack.api.createSolve(solveBody("live-pen")).solve!!

        val patched = stack.api.updatePenalty(created.id, UpdatePenaltyRequest("plus2")).solve
        assertNotNull("PATCH /solves/:id must return a wrapped {solve}", patched)
        assertEquals("plus2", patched!!.penalty)

        val fromHistory = stack.api.solves(event = "3x3").items.first { it.id == created.id }
        assertEquals("the penalty is persisted", "plus2", fromHistory.penalty)
    }

    @Test
    fun `delete removes the solve from history`() = runBlocking {
        LiveApi.registerFresh(stack)
        val created = stack.api.createSolve(solveBody("live-del")).solve!!

        stack.api.deleteSolve(created.id)

        val history = stack.api.solves(event = "3x3")
        assertFalse(
            "the deleted solve is gone from history",
            history.items.any { it.id == created.id },
        )
    }

    @Test
    fun `logout actually revokes - the refresh token is rejected afterwards`() = runBlocking {
        LiveApi.registerFresh(stack)
        val refresh = stack.tokens.refreshToken()!!

        // The production sign-out path: send the refresh token in the body. A body-less logout 400s
        // and never revokes (the exact Flutter/iOS bug).
        stack.api.logout(RefreshRequest(refresh))

        // A revoked refresh token can no longer mint a session. Hit /auth/refresh on a session-less
        // stack so the authenticator does not paper over the 401.
        val fresh = LiveApi.newStack()
        val ex = assertThrows(HttpException::class.java) {
            runBlocking { fresh.api.refresh(RefreshRequest(refresh)) }
        }
        assertEquals("logout revoked the refresh token", 401, ex.code())
    }

    @Test
    fun `body-less logout is rejected by the server - regression guard`() = runBlocking {
        // Proves WHY the body is mandatory: sending an empty refresh reproduces the original 400 the
        // client used to trigger. Keep this so a future "simplification" back to no-body is caught.
        LiveApi.registerFresh(stack)
        val ex = assertThrows(HttpException::class.java) {
            runBlocking { stack.api.logout(RefreshRequest("")) }
        }
        assertEquals("logout requires a non-empty refresh token", 400, ex.code())
    }

    @Test
    fun `me without a token is rejected`() = runBlocking {
        // Session-less stack: the interceptor attaches no Bearer and the authenticator has no refresh
        // to recover with, so the 401 flows straight through.
        val anon = LiveApi.newStack()
        val ex = assertThrows(HttpException::class.java) {
            runBlocking { anon.api.me() }
        }
        assertEquals(401, ex.code())
    }

    @Test
    fun `stats for a fresh user is empty but well-shaped`() = runBlocking {
        LiveApi.registerFresh(stack)

        val stats = stack.api.stats(event = "3x3")
        assertEquals("3x3", stats.event)
        assertEquals(0, stats.solveCount)
        assertNull("no solves yet → best is null", stats.best)
    }
}
