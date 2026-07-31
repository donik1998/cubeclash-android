package com.donik1998.cubeclash.core.network.live

import com.donik1998.cubeclash.core.network.dto.CreateSolveRequest
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Before
import org.junit.Test

/**
 * **Wire-shape** tests against a running `cubeclash-backend` (`/v1`). Where [LiveApiTest] asserts
 * behaviour, this asserts the raw bytes: which keys are present, which are `null`, which are
 * *absent*. This is the half that catches a renamed or leaked field before it reaches production —
 * because it reads the JSON directly rather than through a DTO that would silently drop the
 * difference.
 *
 * Skipped by default. Run command is identical to [LiveApiTest] (swap `--tests '*live.LiveWireTest'`).
 */
class LiveWireTest {

    private lateinit var stack: LiveApi.Stack
    private lateinit var raw: OkHttpClient
    private lateinit var access: String
    private lateinit var userId: String
    private val json: Json = LiveApi.json()

    @Before
    fun setUp() = runBlocking {
        assumeTrue(
            "live suite — run with -Dcubeclash.liveApi=true against a running backend (see header)",
            LiveApi.enabled,
        )
        stack = LiveApi.newStack()
        LiveApi.registerFresh(stack, displayName = "wire_probe")
        access = stack.tokens.accessToken()!!
        userId = stack.api.me().user!!.id
        // A plain client with no auth interceptors (assertions must see exactly what the server
        // sends), but with 429 backoff so the rate limiter cannot flake the suite.
        raw = LiveApi.rawClient()
    }

    /** GETs [path] with the session bearer and returns the parsed JSON object. */
    private fun getObject(path: String): JsonObject {
        val res = raw.newCall(
            Request.Builder()
                .url(LiveApi.baseUrl.trimEnd('/') + path)
                .header("Authorization", "Bearer $access")
                .build(),
        ).execute()
        val body = res.use { it.body?.string().orEmpty() }
        return json.parseToJsonElement(body).jsonObject
    }

    private fun postSolve(clientId: String, timeMs: Long, solvedAt: String) = runBlocking {
        stack.api.createSolve(
            CreateSolveRequest(
                event = "3x3",
                scramble = "R U R' U' F2 L D2 B'",
                scrambleSource = "random",
                timeMs = timeMs,
                penalty = "none",
                solvedAt = solvedAt,
                clientId = clientId,
            ),
        )
    }

    private fun JsonObject.isNullField(key: String): Boolean =
        this[key].let { it != null && it is JsonNull }

    private fun JsonObject.stringOrNull(key: String): String? =
        (this[key] as? JsonElement)?.let { if (it is JsonNull) null else it.jsonPrimitive.contentOrNull }

    @Test
    fun `me is wrapped in user and uses snake_case keys`() {
        val root = getObject("/me")
        assertTrue("GET /me is wrapped in {user}", root.containsKey("user"))
        val user = root["user"]!!.jsonObject
        assertTrue(user.containsKey("id"))
        assertTrue("snake_case display_name", user.containsKey("display_name"))
        assertFalse("no camelCase leak", user.containsKey("displayName"))
    }

    @Test
    fun `stats returns progress and distribution and best_single_ms`() {
        val root = getObject("/stats?event=3x3")
        assertEquals("3x3", root.stringOrNull("event"))
        // The best single is `best_single_ms`, NOT `best` — the wire key the DTO used to miss.
        assertTrue("best_single_ms is present (null for a fresh user)", root.containsKey("best_single_ms"))
        assertFalse("there is no bare `best` key", root.containsKey("best"))
        assertTrue("progress array present", root.containsKey("progress"))
        assertTrue("distribution array present", root.containsKey("distribution"))
    }

    @Test
    fun `public user exposes bests and h2h but never leaks email`() {
        val user = getObject("/users/$userId")["user"]!!.jsonObject
        assertTrue(user.containsKey("best_single_ms"))
        assertTrue("averages are ao5/ao12", user.containsKey("ao5") && user.containsKey("ao12"))
        assertTrue("head_to_head key present", user.containsKey("head_to_head"))
        // Never-raced is null, a distinct state from {0,0}.
        assertTrue("head_to_head is null when the two never raced", user.isNullField("head_to_head"))
        // No email may ever appear on a public profile.
        assertFalse("GET /users/:id must not leak email", user.containsKey("email"))
    }

    @Test
    fun `is_pb is true on exactly the fastest solve`() {
        // Log a fast solve (a PB), then a slower one (not a PB): the server stamps is_pb at insert.
        postSolve("wire-fast", timeMs = 5000, solvedAt = "2026-07-31T04:00:00.000Z")
        postSolve("wire-slow", timeMs = 9000, solvedAt = "2026-07-31T04:01:00.000Z")

        val items = getObject("/solves?event=3x3")["items"] as JsonArray
        val rows = items.map { it.jsonObject }
        val fast = rows.first { it["client_id"]!!.jsonPrimitive.content == "wire-fast" }
        val slow = rows.first { it["client_id"]!!.jsonPrimitive.content == "wire-slow" }
        assertEquals("the fastest solve is a PB", true, fast["is_pb"]!!.jsonPrimitive.content.toBoolean())
        assertEquals("a slower later solve is not a PB", false, slow["is_pb"]!!.jsonPrimitive.content.toBoolean())
    }

    @Test
    fun `POST solves rejects a missing scramble_source`() {
        val res = raw.newCall(
            Request.Builder()
                .url(LiveApi.baseUrl.trimEnd('/') + "/solves")
                .header("Authorization", "Bearer $access")
                .post(
                    """{"event":"3x3","scramble":"R U","time_ms":8137,"penalty":"none","solved_at":"2026-07-31T04:00:00.000Z","client_id":"wire-noSource"}"""
                        .toRequestBody("application/json".toMediaType()),
                )
                .build(),
        ).execute()
        val code = res.code
        val body = res.use { it.body?.string().orEmpty() }
        assertEquals("scramble_source is required", 400, code)
        assertTrue(
            "the error envelope names scramble_source",
            body.contains("scramble_source"),
        )
    }

    @Test
    fun `a multi-line scramble round-trips with newlines intact and untrimmed`() {
        // Leading/trailing space and a real \n between lines: all must survive byte-for-byte.
        val scramble = "  R U R' U'\nF2 L D2 B'  "
        val created = runBlocking {
            stack.api.createSolve(
                CreateSolveRequest(
                    event = "3x3",
                    scramble = scramble,
                    scrambleSource = "random",
                    timeMs = 8137,
                    penalty = "none",
                    solvedAt = "2026-07-31T04:02:00.000Z",
                    clientId = "wire-nl",
                ),
            ).solve!!
        }
        assertEquals("scramble round-trips untrimmed with \\n intact", scramble, created.scramble)
        assertTrue("the newline survived", created.scramble.contains("\n"))
    }

    @Test
    fun `error envelope uses a stable code, not a message`() {
        val res = raw.newCall(
            Request.Builder()
                .url(LiveApi.baseUrl.trimEnd('/') + "/me")
                .build(), // no Authorization
        ).execute()
        val code = res.code
        val body = res.use { it.body?.string().orEmpty() }
        assertEquals(401, code)
        val error = json.parseToJsonElement(body).jsonObject["error"]!!.jsonObject
        assertEquals("branch on code, never message", "unauthorized", error["code"]!!.jsonPrimitive.content)
    }
}
