package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class LeaderboardDtoTest {

    // The same configuration NetworkModule provides in production.
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `full item round-trips including snake_case`() {
        val wire = """
            {
              "rank": 3,
              "user_id": "u-1",
              "display_name": "kian_r",
              "country": "IR",
              "value_ms": 6310,
              "event": "3x3",
              "solved_at": "2026-07-20T09:12:03.000Z"
            }
        """.trimIndent()

        val dto = json.decodeFromString<LeaderboardItemDto>(wire)
        assertEquals(3, dto.rank)
        assertEquals("u-1", dto.userId)
        assertEquals("kian_r", dto.displayName)
        assertEquals("IR", dto.country)
        assertEquals(6310L, dto.valueMs)
        assertEquals("3x3", dto.event)
        assertEquals("2026-07-20T09:12:03.000Z", dto.solvedAt)

        val reencoded = json.decodeFromString<LeaderboardItemDto>(json.encodeToString(dto))
        assertEquals(dto, reencoded)
    }

    @Test
    fun `response round-trips with items, next_cursor and viewer`() {
        val wire = """
            {
              "items": [
                {"rank":1,"user_id":"a","display_name":"a","value_ms":6000,"event":"3x3","solved_at":"2026-07-20T09:12:03.000Z"}
              ],
              "next_cursor": "cursor-2",
              "viewer": {"rank":1204,"user_id":"me","display_name":"me","value_ms":9000,"event":"3x3","solved_at":"2026-07-20T09:12:03.000Z"}
            }
        """.trimIndent()

        val dto = json.decodeFromString<LeaderboardResponseDto>(wire)
        assertEquals(1, dto.items?.size)
        assertEquals("cursor-2", dto.nextCursor)
        assertEquals(1204, dto.viewer?.rank)

        val reencoded = json.decodeFromString<LeaderboardResponseDto>(json.encodeToString(dto))
        assertEquals(dto, reencoded)
    }

    @Test
    fun `empty item object decodes without throwing and every field is null`() {
        val dto = json.decodeFromString<LeaderboardItemDto>("{}")
        assertNull(dto.rank)
        assertNull(dto.userId)
        assertNull(dto.displayName)
        assertNull(dto.country)
        assertNull(dto.valueMs)
        assertNull(dto.event)
        assertNull(dto.solvedAt)
    }

    @Test
    fun `empty response object decodes without throwing`() {
        val dto = json.decodeFromString<LeaderboardResponseDto>("{}")
        assertNull(dto.items)
        assertNull(dto.nextCursor)
        assertNull(dto.viewer)
    }

    @Test
    fun `explicit nulls on the wire decode to null`() {
        val wire = """{"rank":null,"user_id":null,"country":null,"value_ms":null}"""
        val dto = json.decodeFromString<LeaderboardItemDto>(wire)
        assertNull(dto.rank)
        assertNull(dto.userId)
        assertNull(dto.country)
        assertNull(dto.valueMs)
    }
}
