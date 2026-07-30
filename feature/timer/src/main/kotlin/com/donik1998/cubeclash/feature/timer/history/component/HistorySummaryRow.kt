package com.donik1998.cubeclash.feature.timer.history.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.component.StatCard
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.ResultFormatter

/**
 * The three cards at the top of history: best, ao5, ao12 over everything loaded. An em dash
 * stands in for "not enough solves yet" rather than a zero, so a fresh account reads honestly.
 */
@Composable
fun HistorySummaryRow(stats: EventStats, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        StatCard(
            label = "Best",
            value = ResultFormatter.formatAverage(stats.best, stats.event),
            highlighted = true,
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Ao5",
            value = ResultFormatter.formatAverage(stats.ao5, stats.event),
            modifier = Modifier.weight(1f),
        )
        StatCard(
            label = "Ao12",
            value = ResultFormatter.formatAverage(stats.ao12, stats.event),
            modifier = Modifier.weight(1f),
        )
    }
}
