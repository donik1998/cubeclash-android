package com.donik1998.cubeclash.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SolveResultTest {

    @Test
    fun `plus two adds two seconds rather than flagging the display`() {
        val result = SolveResult.Timed(rawTimeMs = 12_340, penalty = Penalty.PLUS_TWO)
        assertEquals(14_340, result.effectiveTimeMs)
        assertEquals(14_340, result.rankingValue)
    }

    @Test
    fun `a DNF always ranks last`() {
        val dnf = SolveResult.Timed(8_000, Penalty.DNF)
        val slow = SolveResult.Timed(600_000, Penalty.NONE)
        assertTrue(dnf > slow)
    }

    @Test
    fun `multi-blind ranks by points then time - 9f12`() {
        val morePoints = SolveResult.MultiBlind(solvedCount = 9, attemptedCount = 10, timeMs = 3_500_000)
        val fasterFewer = SolveResult.MultiBlind(solvedCount = 5, attemptedCount = 5, timeMs = 600_000)
        assertTrue("9/10 outranks 5/5 despite being slower", morePoints < fasterFewer)

        val sameScoreFaster = SolveResult.MultiBlind(9, 10, timeMs = 1_000_000)
        assertTrue(sameScoreFaster < morePoints)
    }

    @Test
    fun `multi-blind auto-DNFs below two solved or a non-positive score`() {
        assertTrue(SolveResult.MultiBlind(solvedCount = 1, attemptedCount = 2, timeMs = 100).isDnf)
        // 3 solved of 6 scores 3 - 3 = 0 points.
        assertTrue(SolveResult.MultiBlind(solvedCount = 3, attemptedCount = 6, timeMs = 100).isDnf)
        assertTrue(!SolveResult.MultiBlind(solvedCount = 4, attemptedCount = 6, timeMs = 100).isDnf)
    }

    @Test
    fun `fewest moves ranks on solution length, shortest first`() {
        assertTrue(SolveResult.MoveCount(24) < SolveResult.MoveCount(31))
        assertTrue(SolveResult.MoveCount(null).isDnf)
    }
}
