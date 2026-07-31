package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * ⚠️ `GET /tournaments` and `GET /tournaments/{id}` are unimplemented server-side (404); these
 * fixtures are hand-written to the API Design doc / Flutter client shape, NOT bytes a server
 * produced. Re-verify once the routes exist.
 */
class TournamentDtoTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
        coerceInputValues = true
    }

    @Test
    fun `a full tournament page round-trips including snake_case`() {
        val wire = """
            {
              "items": [
                {
                  "id": "weekly-333",
                  "name": "Global Weekly · 3×3",
                  "event": "3x3",
                  "status": "live",
                  "entrants": 48,
                  "capacity": 64,
                  "starts_at": "2026-07-31T09:00:00+00:00",
                  "description": "Open bracket.",
                  "registered": true
                }
              ],
              "next_cursor": "c2"
            }
        """.trimIndent()

        val dto = json.decodeFromString<TournamentListResponseDto>(wire)
        val t = dto.items!![0]!!
        assertEquals("weekly-333", t.id)
        assertEquals("Global Weekly · 3×3", t.name)
        assertEquals("3x3", t.event)
        assertEquals("live", t.status)
        assertEquals(48, t.entrants)
        assertEquals(64, t.capacity)
        assertEquals("2026-07-31T09:00:00+00:00", t.startsAt)
        assertEquals(true, t.registered)
        assertEquals("c2", dto.nextCursor)

        val reencoded = json.decodeFromString<TournamentListResponseDto>(json.encodeToString(dto))
        assertEquals(dto, reencoded)
    }

    @Test
    fun `a detail with a bracket round-trips including nested snake_case`() {
        val wire = """
            {
              "tournament": {"id": "weekly-333", "name": "Weekly", "event": "3x3", "status": "live"},
              "rounds": [
                {
                  "name": "Final",
                  "matches": [
                    {"player_a": "kian_r", "player_b": "owen_p", "time_a_ms": 6120, "time_b_ms": 6540, "winner": "A"}
                  ]
                }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<TournamentDetailResponseDto>(wire)
        assertEquals("weekly-333", dto.tournament?.id)
        val match = dto.rounds!![0]!!.matches!![0]!!
        assertEquals("kian_r", match.playerA)
        assertEquals("owen_p", match.playerB)
        assertEquals(6120L, match.timeAMs)
        assertEquals(6540L, match.timeBMs)
        assertEquals("A", match.winner)

        val reencoded = json.decodeFromString<TournamentDetailResponseDto>(json.encodeToString(dto))
        assertEquals(dto, reencoded)
    }

    @Test
    fun `empty objects decode without throwing and every field is null`() {
        // The house-rule empty-object test, for every tournament DTO.
        val t = json.decodeFromString<TournamentDto>("{}")
        assertNull(t.id)
        assertNull(t.name)
        assertNull(t.event)
        assertNull(t.status)
        assertNull(t.entrants)
        assertNull(t.capacity)
        assertNull(t.startsAt)
        assertNull(t.description)
        assertNull(t.registered)

        val page = json.decodeFromString<TournamentListResponseDto>("{}")
        assertNull(page.items)
        assertNull(page.nextCursor)

        val detail = json.decodeFromString<TournamentDetailResponseDto>("{}")
        assertNull(detail.tournament)
        assertNull(detail.rounds)

        val round = json.decodeFromString<TournamentRoundDto>("{}")
        assertNull(round.name)
        assertNull(round.matches)

        val match = json.decodeFromString<TournamentMatchDto>("{}")
        assertNull(match.playerA)
        assertNull(match.playerB)
        assertNull(match.timeAMs)
        assertNull(match.timeBMs)
        assertNull(match.winner)
    }
}
