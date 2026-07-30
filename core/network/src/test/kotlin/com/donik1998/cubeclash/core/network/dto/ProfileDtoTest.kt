package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class ProfileDtoTest {

    // The same configuration NetworkModule provides in production.
    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `full profile round-trips including snake_case`() {
        val wire = """
            {
              "user": {
                "id": "0e5b-uuid",
                "display_name": "cuber_98",
                "country": "UZ",
                "elo": 1180
              },
              "rank": {
                "event": "3x3",
                "metric": "single",
                "scope": "global",
                "position": 1204
              },
              "stats": {
                "best_single_ms": 8420,
                "best_single_event": "3x3",
                "total_solves": 3204,
                "win_rate": 0.68,
                "wins": 217,
                "losses": 102
              },
              "friend_count": 48
            }
        """.trimIndent()

        val dto = json.decodeFromString<ProfileResponseDto>(wire)
        assertEquals("0e5b-uuid", dto.user?.id)
        assertEquals("cuber_98", dto.user?.displayName)
        assertEquals("UZ", dto.user?.country)
        assertEquals(1180, dto.user?.elo)
        assertEquals(1204, dto.rank?.position)
        assertEquals("global", dto.rank?.scope)
        assertEquals(8420L, dto.stats?.bestSingleMs)
        assertEquals("3x3", dto.stats?.bestSingleEvent)
        assertEquals(3204, dto.stats?.totalSolves)
        assertEquals(0.68, dto.stats?.winRate!!, 0.0001)
        assertEquals(217, dto.stats?.wins)
        assertEquals(102, dto.stats?.losses)
        assertEquals(48, dto.friendCount)

        val reencoded = json.decodeFromString<ProfileResponseDto>(json.encodeToString(dto))
        assertEquals(dto, reencoded)
    }

    @Test
    fun `empty object decodes without throwing and every field is null`() {
        val dto = json.decodeFromString<ProfileResponseDto>("{}")
        assertNull(dto.user)
        assertNull(dto.rank)
        assertNull(dto.stats)
        assertNull(dto.friendCount)
    }

    @Test
    fun `empty nested objects decode without throwing and every field is null`() {
        val wire = """{"user":{},"rank":{},"stats":{}}"""
        val dto = json.decodeFromString<ProfileResponseDto>(wire)
        assertNull(dto.user?.id)
        assertNull(dto.user?.displayName)
        assertNull(dto.user?.country)
        assertNull(dto.user?.elo)
        assertNull(dto.rank?.position)
        assertNull(dto.rank?.event)
        assertNull(dto.stats?.bestSingleMs)
        assertNull(dto.stats?.bestSingleEvent)
        assertNull(dto.stats?.totalSolves)
        assertNull(dto.stats?.winRate)
        assertNull(dto.stats?.wins)
        assertNull(dto.stats?.losses)
    }

    @Test
    fun `explicit nulls on the wire decode to null`() {
        val wire = """
            {
              "user": {"id": null, "display_name": null, "country": null, "elo": null},
              "rank": null,
              "stats": {"best_single_ms": null, "win_rate": null},
              "friend_count": null
            }
        """.trimIndent()
        val dto = json.decodeFromString<ProfileResponseDto>(wire)
        assertNull(dto.user?.id)
        assertNull(dto.user?.displayName)
        assertNull(dto.user?.country)
        assertNull(dto.user?.elo)
        assertNull(dto.rank)
        assertNull(dto.stats?.bestSingleMs)
        assertNull(dto.stats?.winRate)
        assertNull(dto.friendCount)
    }
}
