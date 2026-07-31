package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Offline regression guards for the response-envelope bugs the live suite found: the server wraps
 * `GET /me` in `{"user": …}` and `POST /solves` in `{"solve": …}`, and `GET /stats` names the best
 * single `best_single_ms`. These parse the exact bytes a running backend produced, so they hold the
 * line even when the live gate is off. They do NOT need a server.
 */
class EnvelopeDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `GET me is wrapped in user`() {
        // Captured from http://localhost:3100/v1/me.
        val wire = """
            {"user":{"id":"fdd95273-9585-49fa-b0d9-cfa14da760cd","email":"probe@ex.com",
            "display_name":"probeuser","country":null,"elo":1000,"created_at":"2026-07-31T04:24:18.387Z"}}
        """.trimIndent()

        val me = json.decodeFromString<MeResponse>(wire)
        assertEquals("probeuser", me.user?.displayName)
        assertEquals(1000, me.user?.elo)
    }

    @Test
    fun `MeResponse decodes empty object to a null user without throwing`() {
        assertNull(json.decodeFromString<MeResponse>("{}").user)
    }

    @Test
    fun `POST solves is wrapped in solve with newlines intact`() {
        // Captured from POST http://localhost:3100/v1/solves.
        val wire =
            "{\"solve\":{\"id\":\"8b4f975a\",\"user_id\":\"86b1d03f\",\"event\":\"3x3\"," +
                "\"scramble\":\"R U R' U'\\nF2 L D2 B'\",\"scramble_source\":\"random\"," +
                "\"time_ms\":8137,\"penalty\":\"none\",\"solved_at\":\"2026-07-31T04:00:00.000Z\"," +
                "\"client_id\":\"probe-s1\",\"is_pb\":true}}"

        val res = json.decodeFromString<SolveResponse>(wire)
        assertEquals("8b4f975a", res.solve?.id)
        assertTrue("\\n round-trips", res.solve?.scramble?.contains("\n") == true)
        assertEquals("server-owned is_pb is parsed", true, res.solve?.isPb)
    }

    @Test
    fun `SolveResponse decodes empty object to a null solve without throwing`() {
        assertNull(json.decodeFromString<SolveResponse>("{}").solve)
    }

    @Test
    fun `stats best comes from best_single_ms not best`() {
        // Captured from http://localhost:3100/v1/stats?event=3x3 with a solve on file.
        val wire = """{"event":"3x3","best_single_ms":6289,"ao5":null,"ao12":null,
            "ao100":null,"session_average":null,"pb_count":1,"solve_count":1,
            "progress":[],"distribution":[]}""".trimIndent()

        val stats = json.decodeFromString<StatsDto>(wire)
        assertEquals("wire key is best_single_ms, not best", 6289L, stats.best)
    }

    @Test
    fun `StatsDto decodes empty object without throwing`() {
        val stats = json.decodeFromString<StatsDto>("{}")
        assertNull(stats.event)
        assertNull(stats.best)
        assertEquals(0, stats.solveCount)
    }
}
