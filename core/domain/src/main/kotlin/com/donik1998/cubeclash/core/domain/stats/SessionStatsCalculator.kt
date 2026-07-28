package com.donik1998.cubeclash.core.domain.stats

import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.ResultKind
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.WcaEvent
import javax.inject.Inject

/**
 * Turns a session (newest first) into the three cards under the timer.
 *
 * Which three is [WcaEvent.format]'s call, not this class's — it computes everything cheap
 * and lets the format decide what gets rendered.
 */
class SessionStatsCalculator @Inject constructor() {

    operator fun invoke(event: WcaEvent, solves: List<Solve>): EventStats {
        val results = solves.map { it.result }
        // A mean of 28.33 moves must survive as 28.33, not 28.
        val scale = if (event.resultKind == ResultKind.MOVE_COUNT) 100L else 1L

        return EventStats(
            event = event,
            best = Averages.best(results),
            ao5 = Averages.average(results, count = 5, scale = scale),
            ao12 = Averages.average(results, count = 12, scale = scale),
            ao100 = Averages.average(results, count = 100, scale = scale),
            mo3 = Averages.mean(results, count = 3, scale = scale),
            last = results.firstOrNull()?.rankingValue,
            solveCount = solves.size,
        )
    }
}
