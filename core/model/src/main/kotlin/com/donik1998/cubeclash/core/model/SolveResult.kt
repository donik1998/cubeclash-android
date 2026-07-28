package com.donik1998.cubeclash.core.model

/**
 * What an attempt actually produced.
 *
 * `Solve.timeMs` keeps meaning *the attempt's duration* for every event — Fewest Moves and
 * Multi-Blind **are** timed, they are just not *ranked* on the clock. So the result is
 * modelled separately from the duration, and ranking always goes through [rankingValue],
 * never through a raw time.
 */
sealed interface SolveResult : Comparable<SolveResult> {

    val isDnf: Boolean

    /** Lower is better. [DNF_RANK] sorts last, which is what a DNF means. */
    val rankingValue: Long

    override fun compareTo(other: SolveResult): Int = rankingValue.compareTo(other.rankingValue)

    /** Fifteen of the seventeen events. */
    data class Timed(val rawTimeMs: Long, val penalty: Penalty = Penalty.NONE) : SolveResult {
        /** The time that counts: `+2` is two seconds *added*, not a flag on the display. */
        val effectiveTimeMs: Long
            get() = if (penalty == Penalty.PLUS_TWO) rawTimeMs + 2_000L else rawTimeMs

        override val isDnf: Boolean get() = penalty == Penalty.DNF
        override val rankingValue: Long get() = if (isDnf) DNF_RANK else effectiveTimeMs
    }

    /** Fewest Moves — the result is a solution length, so a shorter one wins. */
    data class MoveCount(val moves: Int?) : SolveResult {
        override val isDnf: Boolean get() = moves == null
        override val rankingValue: Long get() = moves?.toLong() ?: DNF_RANK
    }

    /**
     * Multi-Blind. Ranked by **points, then time** (9f12), where points = solved − unsolved.
     * An attempt below two solved cubes, or with a non-positive score, is **automatically a
     * DNF** — that rule lives here rather than in any caller.
     */
    data class MultiBlind(
        val solvedCount: Int,
        val attemptedCount: Int,
        val timeMs: Long,
    ) : SolveResult {
        val points: Int get() = solvedCount - (attemptedCount - solvedCount)

        override val isDnf: Boolean
            get() = solvedCount < 2 || points <= 0 || attemptedCount < 2

        override val rankingValue: Long
            get() = if (isDnf) DNF_RANK else (MAX_POINTS - points) * POINT_WEIGHT + timeMs
    }

    companion object {
        const val DNF_RANK: Long = Long.MAX_VALUE
        private const val MAX_POINTS = 200L

        /** One hour is the Multi-Blind cap (H1b), so a point is always worth more than any time. */
        private const val POINT_WEIGHT = 3_600_001L
    }
}
