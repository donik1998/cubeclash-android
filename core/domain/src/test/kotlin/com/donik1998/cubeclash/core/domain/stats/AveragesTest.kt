package com.donik1998.cubeclash.core.domain.stats

import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.SolveResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AveragesTest {

    private fun timed(vararg ms: Long) = ms.map { SolveResult.Timed(it) }

    @Test
    fun `an average needs a full window before it prints anything`() {
        assertNull(Averages.average(timed(10_000, 11_000, 12_000, 13_000), count = 5))
    }

    @Test
    fun `average of five drops the best and the worst - 9f3`() {
        // 8, 9, 10, 11, 30 -> drop 8 and 30 -> mean(9, 10, 11) = 10
        val results = timed(8_000, 9_000, 10_000, 11_000, 30_000)
        assertEquals(10_000L, Averages.average(results, count = 5))
    }

    @Test
    fun `one DNF is absorbed as the worst attempt`() {
        val results = listOf(
            SolveResult.Timed(9_000),
            SolveResult.Timed(10_000),
            SolveResult.Timed(11_000),
            SolveResult.Timed(12_000),
            SolveResult.Timed(8_000, Penalty.DNF),
        )
        // 9, 10, 11, 12, DNF -> drop 9 and the DNF -> mean(10, 11, 12) = 11
        assertEquals(11_000L, Averages.average(results, count = 5))
    }

    @Test
    fun `two DNFs make the whole average a DNF`() {
        val results = listOf(
            SolveResult.Timed(9_000),
            SolveResult.Timed(10_000),
            SolveResult.Timed(11_000),
            SolveResult.Timed(1_000, Penalty.DNF),
            SolveResult.Timed(2_000, Penalty.DNF),
        )
        assertEquals(SolveResult.DNF_RANK, Averages.average(results, count = 5))
    }

    @Test
    fun `a mean trims nothing, so a single DNF poisons it`() {
        val results = listOf(
            SolveResult.Timed(60_000),
            SolveResult.Timed(70_000),
            SolveResult.Timed(80_000, Penalty.DNF),
        )
        assertEquals(SolveResult.DNF_RANK, Averages.mean(results, count = 3))
    }

    @Test
    fun `mean of three is the plain arithmetic mean`() {
        assertEquals(70_000L, Averages.mean(timed(60_000, 70_000, 80_000), count = 3))
    }

    @Test
    fun `fewest moves means scale to centi-moves so the decimals survive`() {
        val results = listOf(SolveResult.MoveCount(28), SolveResult.MoveCount(29), SolveResult.MoveCount(28))
        // (28 + 29 + 28) / 3 = 28.333... -> 2833 centi-moves
        assertEquals(2833L, Averages.mean(results, count = 3, scale = 100L))
    }

    @Test
    fun `a session of nothing but DNFs has no best single`() {
        assertNull(Averages.best(listOf(SolveResult.Timed(9_000, Penalty.DNF))))
    }

    @Test
    fun `plus two counts towards the average at its penalised value`() {
        val results = timed(10_000, 10_000, 10_000, 10_000).toMutableList().apply {
            add(SolveResult.Timed(10_000, Penalty.PLUS_TWO))
        }
        // 10, 10, 10, 10, 12 -> drop 10 and 12 -> mean(10, 10, 10) = 10
        assertEquals(10_000L, Averages.average(results, count = 5))
    }
}
