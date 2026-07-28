package com.donik1998.cubeclash.core.domain.stats

import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.SessionStatKind
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.SolveResult
import com.donik1998.cubeclash.core.model.WcaEvent
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class SessionStatsCalculatorTest {

    private val calculator = SessionStatsCalculator()

    private fun session(event: WcaEvent, vararg timesMs: Long): List<Solve> =
        timesMs.mapIndexed { index, time ->
            Solve(
                id = "s$index",
                clientId = "s$index",
                event = event,
                scramble = Scramble.parse("R U R'", ScrambleNotation.FACE_TURN),
                scrambleSource = ScrambleSource.RANDOM,
                timeMs = time,
                penalty = Penalty.NONE,
                solvedAt = Instant.EPOCH,
            )
        }

    @Test
    fun `a 3x3 session shows best, ao5 and ao12`() {
        assertEquals(
            listOf(SessionStatKind.BEST, SessionStatKind.AO5, SessionStatKind.AO12),
            WcaEvent.THREE.format.sessionStats,
        )
    }

    @Test
    fun `a 6x6 session swaps ao12 for the mean it is actually ranked on`() {
        assertEquals(
            listOf(SessionStatKind.BEST, SessionStatKind.MO3, SessionStatKind.AO5),
            WcaEvent.SIX.format.sessionStats,
        )
    }

    @Test
    fun `stats fill in as the session grows`() {
        val stats = calculator(WcaEvent.THREE, session(WcaEvent.THREE, 11_000, 12_000, 10_000))
        assertEquals(10_000L, stats.best)
        assertEquals(11_000L, stats.mo3)
        // Five solves are needed before an ao5 exists, and it says so rather than guessing.
        assertNull(stats.ao5)
        assertEquals(3, stats.solveCount)
    }

    @Test
    fun `the newest solve is the one reported as last`() {
        val stats = calculator(WcaEvent.THREE, session(WcaEvent.THREE, 9_870, 12_000))
        assertEquals(9_870L, stats.last)
    }

    @Test
    fun `a multi-blind attempt below two solved cubes auto-DNFs - 9f12`() {
        // The helper leaves solved/attempted unset, which is exactly the 0-of-0 case the
        // auto-DNF rule exists for: Multi-Blind never ranks on the clock alone.
        val stats = calculator(WcaEvent.MBLD, session(WcaEvent.MBLD, 3_000_000))
        assertEquals(SolveResult.DNF_RANK, stats.last)
    }
}
