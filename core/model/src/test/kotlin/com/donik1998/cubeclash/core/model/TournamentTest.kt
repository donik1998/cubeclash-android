package com.donik1998.cubeclash.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TournamentTest {

    private fun tournament(entrants: Int, capacity: Int) = Tournament(
        id = "t-1",
        name = "T",
        event = WcaEvent.THREE,
        status = TournamentStatus.LIVE,
        entrants = entrants,
        capacity = capacity,
        startsAt = null,
        description = "",
    )

    @Test
    fun `isFull is false below capacity, true at and above it`() {
        assertFalse(tournament(entrants = 63, capacity = 64).isFull)
        assertTrue(tournament(entrants = 64, capacity = 64).isFull) // the boundary
        assertTrue(tournament(entrants = 65, capacity = 64).isFull)
    }

    @Test
    fun `a zero-capacity bracket reads as full and never divides by anything`() {
        assertTrue(tournament(entrants = 0, capacity = 0).isFull)
    }

    @Test
    fun `TournamentStatus fromWire is lenient and never guesses a neighbour`() {
        assertEquals(TournamentStatus.LIVE, TournamentStatus.fromWire("live"))
        assertEquals(TournamentStatus.UPCOMING, TournamentStatus.fromWire("upcoming"))
        assertEquals(TournamentStatus.FINISHED, TournamentStatus.fromWire("finished"))
        assertEquals(TournamentStatus.UNKNOWN, TournamentStatus.fromWire("seeding"))
        assertEquals(TournamentStatus.UNKNOWN, TournamentStatus.fromWire(null))
    }

    @Test
    fun `a match is played only once it has a winner`() {
        assertFalse(TournamentMatch(playerA = "a", playerB = "b").isPlayed)
        assertTrue(TournamentMatch(playerA = "a", playerB = "b", winner = "A").isPlayed)
    }
}
