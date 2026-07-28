package com.donik1998.cubeclash.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ResultFormatterTest {

    @Test
    fun `sub-minute times print hundredths`() {
        assertEquals("9.87", ResultFormatter.format(SolveResult.Timed(9_870)))
        assertEquals("0.05", ResultFormatter.format(SolveResult.Timed(50)))
    }

    @Test
    fun `minutes print as m ss hundredths`() {
        assertEquals("1:23.45", ResultFormatter.format(SolveResult.Timed(83_450)))
    }

    @Test
    fun `ten minutes and over truncate to whole seconds - 9f2`() {
        // Printing 10:00.00 would claim a precision the Regulations do not recognise.
        assertEquals("10:00", ResultFormatter.format(SolveResult.Timed(600_000)))
        assertEquals("1:05:03", ResultFormatter.format(SolveResult.Timed(3_903_990)))
    }

    @Test
    fun `multi-blind always truncates to seconds whatever its length - 9f2`() {
        val result = SolveResult.MultiBlind(solvedCount = 11, attemptedCount = 13, timeMs = 3_262_990)
        assertEquals("11/13 in 54:22", ResultFormatter.format(result))
    }

    @Test
    fun `a plus two is marked on the value it produced`() {
        assertEquals("11.87+", ResultFormatter.format(SolveResult.Timed(9_870, Penalty.PLUS_TWO)))
    }

    @Test
    fun `fewest moves means keep two decimals`() {
        assertEquals("28.33", ResultFormatter.formatAverage(2833, WcaEvent.FMC))
        assertEquals("DNF", ResultFormatter.formatAverage(SolveResult.DNF_RANK, WcaEvent.THREE))
        assertEquals("—", ResultFormatter.formatAverage(null, WcaEvent.THREE))
    }
}
