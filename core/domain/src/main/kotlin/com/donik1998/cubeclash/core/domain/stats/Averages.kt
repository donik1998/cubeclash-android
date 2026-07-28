package com.donik1998.cubeclash.core.domain.stats

import com.donik1998.cubeclash.core.model.SolveResult
import kotlin.math.roundToLong

/**
 * WCA averaging, in one place.
 *
 * The rules that bite:
 *  - an **average** (9f3) drops the best and the worst attempt and means the rest — so a
 *    single DNF is *absorbed* as the worst attempt, while a second DNF makes the whole
 *    average a DNF;
 *  - a **mean** (9f2) trims nothing, so any DNF poisons it outright;
 *  - values are in ranking units, and Fewest Moves means are scaled to *centi-moves* so a
 *    mean of 28.33 survives as `2833` rather than rounding to 28.
 */
object Averages {

    /** WCA average: trim one best + one worst, mean the middle. */
    fun average(results: List<SolveResult>, count: Int, scale: Long = 1L): Long? {
        if (results.size < count) return null
        val window = results.take(count)
        val dnfCount = window.count { it.isDnf }
        if (dnfCount > 1) return SolveResult.DNF_RANK

        val sorted = window.map { it.rankingValue }.sorted()
        val trimmed = sorted.subList(1, sorted.size - 1)
        if (trimmed.any { it == SolveResult.DNF_RANK }) return SolveResult.DNF_RANK
        return mean(trimmed, scale)
    }

    /** Mean of N: no trimming, so a single DNF is fatal. */
    fun mean(results: List<SolveResult>, count: Int, scale: Long = 1L): Long? {
        if (results.size < count) return null
        val window = results.take(count)
        if (window.any { it.isDnf }) return SolveResult.DNF_RANK
        return mean(window.map { it.rankingValue }, scale)
    }

    /** Best single. A session of nothing but DNFs has no best, and says so. */
    fun best(results: List<SolveResult>): Long? =
        results.filterNot { it.isDnf }.minOfOrNull { it.rankingValue }

    private fun mean(values: List<Long>, scale: Long): Long =
        (values.sumOf { it } * scale.toDouble() / values.size).roundToLong()
}
