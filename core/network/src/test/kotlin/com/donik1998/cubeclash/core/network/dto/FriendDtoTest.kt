package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ⚠️ `GET /friends` is unimplemented server-side (404); these fixtures are hand-written to the API
 * Design doc's roadmap shape, NOT bytes a server produced. Re-verify once the route exists.
 */
class FriendDtoTest {

    // The same configuration NetworkModule provides in production.
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `a full friends page round-trips including snake_case`() {
        val wire = """
            {
              "items": [
                {
                  "user_id": "f-kian",
                  "display_name": "kian_r",
                  "status": "accepted",
                  "country": "IR",
                  "avatar_url": "https://cdn.example/kian.png",
                  "best_single_ms": 6310,
                  "incoming": false
                },
                {
                  "user_id": "f-aiko",
                  "display_name": "aiko_m",
                  "status": "pending",
                  "incoming": true
                }
              ],
              "next_cursor": "c2"
            }
        """.trimIndent()

        val dto = json.decodeFromString<FriendListResponseDto>(wire)
        assertEquals(2, dto.items?.size)
        val kian = dto.items!![0]!!
        assertEquals("f-kian", kian.userId)
        assertEquals("kian_r", kian.displayName)
        assertEquals("accepted", kian.status)
        assertEquals("IR", kian.country)
        assertEquals("https://cdn.example/kian.png", kian.avatarUrl)
        assertEquals(6310L, kian.bestSingleMs)
        assertEquals(false, kian.incoming)
        assertEquals(true, dto.items!![1]!!.incoming)
        assertEquals("c2", dto.nextCursor)

        val reencoded = json.decodeFromString<FriendListResponseDto>(json.encodeToString(dto))
        assertEquals(dto, reencoded)
    }

    @Test
    fun `empty object decodes without throwing and every field is null`() {
        // The house-rule empty-object test.
        val row = json.decodeFromString<FriendDto>("{}")
        assertNull(row.userId)
        assertNull(row.displayName)
        assertNull(row.status)
        assertNull(row.country)
        assertNull(row.avatarUrl)
        assertNull(row.bestSingleMs)
        assertNull(row.incoming)

        val page = json.decodeFromString<FriendListResponseDto>("{}")
        assertNull(page.items)
        assertNull(page.nextCursor)
    }

    @Test
    fun `unknown keys are ignored`() {
        val row = json.decodeFromString<FriendDto>(
            """{"user_id":"x","display_name":"y","mutual_races":42}""",
        )
        assertEquals("x", row.userId)
        assertTrue(row.status == null)
    }
}
