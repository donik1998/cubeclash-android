package com.donik1998.cubeclash.feature.timer.history

import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.Solve
import java.time.LocalDate

/**
 * A day's worth of solves plus the two aggregates the header shows.
 *
 * [best] and [count] both come out of `SessionStatsCalculator` run over exactly this day's solves,
 * so the header never disagrees with the rows beneath it.
 */
data class SolveDayGroup(
    val date: LocalDate,
    val solves: List<Solve>,
    val stats: EventStats,
) {
    val count: Int get() = solves.size
    val best: Long? get() = stats.best
}

/**
 * The Session & History screen renders exactly one of these per frame.
 *
 * The list is modelled as a small state machine — Loading / Error / Content — so the body's
 * `when` is exhaustive and every state (including the empty one, which is a *Content* with no
 * groups) is reachable from a preview or a test with fixed data.
 */
sealed interface SessionHistoryUiState {

    data object Loading : SessionHistoryUiState

    /** A dead end: the first load failed with nothing to show. Offers a retry. */
    data class Error(val message: String?) : SessionHistoryUiState

    /**
     * The list resolved. [summary] is the best/ao5/ao12 over everything loaded; [groups] is the
     * per-day breakdown, newest day first. [isEmpty] is the "no solves yet" case.
     */
    data class Content(
        val summary: EventStats,
        val groups: List<SolveDayGroup>,
    ) : SessionHistoryUiState {
        val isEmpty: Boolean get() = groups.isEmpty()
    }
}
