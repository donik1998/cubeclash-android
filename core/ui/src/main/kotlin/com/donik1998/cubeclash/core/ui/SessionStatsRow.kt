package com.donik1998.cubeclash.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.component.StatCard
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.SessionStatKind

/**
 * Three cards, chosen by the event's competition format — `best · ao5 · ao12` for 3×3,
 * `best · mo3 · ao5` for 6×6, `best · last · solves` for Multi-Blind.
 */
@Composable
fun SessionStatsRow(stats: EventStats, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        stats.event.format.sessionStats.forEach { kind ->
            val raw = stats.value(kind)
            StatCard(
                label = kind.label,
                value = when (kind) {
                    SessionStatKind.COUNT -> raw?.toString() ?: "0"
                    else -> ResultFormatter.formatAverage(raw, stats.event)
                },
                highlighted = kind == SessionStatKind.BEST,
                modifier = Modifier.weight(1f),
            )
        }
    }
}
