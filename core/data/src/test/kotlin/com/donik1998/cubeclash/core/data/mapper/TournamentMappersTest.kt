package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.TournamentDetailResponseDto
import com.donik1998.cubeclash.core.network.dto.TournamentDto
import com.donik1998.cubeclash.core.network.dto.TournamentListResponseDto
import com.donik1998.cubeclash.core.network.dto.TournamentMatchDto
import com.donik1998.cubeclash.core.network.dto.TournamentRoundDto
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentMappersTest {

    private fun dto(
        id: String? = "weekly-333",
        name: String? = "Global Weekly",
        event: String? = "3x3",
        status: String? = "live",
        entrants: Int? = 48,
        capacity: Int? = 64,
        startsAt: String? = "2026-07-31T09:00:00+00:00",
        description: String? = "Open bracket.",
        registered: Boolean? = true,
    ) = TournamentDto(id, name, event, status, entrants, capacity, startsAt, description, registered)

    @Test
    fun `a full payload maps to the domain`() {
        val t = dto().toDomain()!!
        assertEquals("weekly-333", t.id)
        assertEquals("Global Weekly", t.name)
        assertEquals(WcaEvent.THREE, t.event)
        assertEquals(TournamentStatus.LIVE, t.status)
        assertEquals(48, t.entrants)
        assertEquals(64, t.capacity)
        assertEquals(Instant.parse("2026-07-31T09:00:00+00:00"), t.startsAt)
        assertEquals("Open bracket.", t.description)
        assertTrue(t.registered)
    }

    @Test
    fun `an all-null card is dropped for missing identity`() {
        assertNull(TournamentDto().toDomain())
    }

    @Test
    fun `a card with a blank id or name is dropped`() {
        assertNull(dto(id = "  ").toDomain())
        assertNull(dto(name = null).toDomain())
    }

    @Test
    fun `neutral defaults fill in for missing counts, description and registered`() {
        val t = dto(
            entrants = null,
            capacity = null,
            description = null,
            registered = null,
        ).toDomain()!!
        assertEquals(0, t.entrants)
        assertEquals(0, t.capacity)
        assertEquals("", t.description)
        assertFalse(t.registered)
    }

    @Test
    fun `an unknown status maps to UNKNOWN and an unknown event degrades to 3x3`() {
        val t = dto(status = "seeding", event = "unobtainium").toDomain()!!
        assertEquals(TournamentStatus.UNKNOWN, t.status)
        assertEquals(WcaEvent.THREE, t.event) // fromId is lenient
    }

    @Test
    fun `a missing or unparseable starts_at stays null rather than fabricating 1970`() {
        assertNull(dto(startsAt = null).toDomain()!!.startsAt)
        assertNull(dto(startsAt = "not-a-date").toDomain()!!.startsAt)
    }

    @Test
    fun `isFull is derived from the counts, not the wire`() {
        // Boundary: entrants == capacity is full; one under is not.
        assertTrue(dto(entrants = 64, capacity = 64).toDomain()!!.isFull)
        assertFalse(dto(entrants = 63, capacity = 64).toDomain()!!.isFull)
        // Over-capacity (should not happen server-side, but must not lie): still full.
        assertTrue(dto(entrants = 65, capacity = 64).toDomain()!!.isFull)
    }

    @Test
    fun `one bad card does not blank the list`() {
        val page = TournamentListResponseDto(
            items = listOf(
                dto(id = "a", name = "a"),
                TournamentDto(), // dropped
                null, // dropped
                dto(id = "b", name = "b"),
            ),
        )
        val list = page.toDomain()
        assertEquals(listOf("a", "b"), list.map { it.id })
    }

    @Test
    fun `a detail maps its header and bracket, dropping un-renderable rounds and matches`() {
        val response = TournamentDetailResponseDto(
            tournament = dto(),
            rounds = listOf(
                TournamentRoundDto(
                    name = "Final",
                    matches = listOf(
                        TournamentMatchDto("kian_r", "owen_p", 6120, 6540, "A"),
                        TournamentMatchDto(playerA = "x", playerB = null), // match dropped
                        null,
                    ),
                ),
                TournamentRoundDto(name = null, matches = emptyList()), // round dropped
                null,
            ),
        )
        val detail = response.toDomain()!!
        assertEquals("weekly-333", detail.tournament.id)
        assertEquals(1, detail.rounds.size)
        assertEquals("Final", detail.rounds[0].name)
        assertEquals(1, detail.rounds[0].matches.size)
        val match = detail.rounds[0].matches[0]
        assertEquals("kian_r", match.playerA)
        assertEquals(6120L, match.timeAMs)
        assertEquals("A", match.winner)
        assertTrue(match.isPlayed)
    }

    @Test
    fun `a detail whose header has no identity is dropped`() {
        assertNull(TournamentDetailResponseDto(tournament = TournamentDto()).toDomain())
        assertNull(TournamentDetailResponseDto(tournament = null).toDomain())
    }

    @Test
    fun `an unplayed match keeps null times and is not marked played`() {
        val response = TournamentDetailResponseDto(
            tournament = dto(),
            rounds = listOf(
                TournamentRoundDto(
                    name = "Final",
                    matches = listOf(TournamentMatchDto(playerA = "a", playerB = "b")),
                ),
            ),
        )
        val match = response.toDomain()!!.rounds[0].matches[0]
        assertNull(match.timeAMs)
        assertNull(match.timeBMs)
        assertNull(match.winner)
        assertFalse(match.isPlayed)
    }
}
