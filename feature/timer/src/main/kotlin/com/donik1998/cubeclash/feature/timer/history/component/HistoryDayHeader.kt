package com.donik1998.cubeclash.feature.timer.history.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.ResultFormatter
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

/**
 * A day divider: a relative label — TODAY / YESTERDAY / "19 JUL" — with the day's solve count and
 * best time. The label reads relative because a session is a recent thing; anything older than
 * yesterday drops to an absolute date rather than an ever-growing "N days ago".
 */
@Composable
fun HistoryDayHeader(
    date: LocalDate,
    count: Int,
    best: Long?,
    modifier: Modifier = Modifier,
    today: LocalDate = LocalDate.now(),
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = dayLabel(date, today),
            style = CubeClashTheme.typography.overline,
            color = CubeClashTheme.colors.textMuted,
        )
        Text(
            text = "· $count ${if (count == 1) "solve" else "solves"}",
            style = CubeClashTheme.typography.caption,
            color = CubeClashTheme.colors.textMuted,
        )
        if (best != null) {
            Text(
                text = "· best ${ResultFormatter.formatDuration(best)}",
                style = CubeClashTheme.typography.caption.copy(fontFeatureSettings = "tnum"),
                color = CubeClashTheme.colors.textMuted,
            )
        }
    }
}

private fun dayLabel(date: LocalDate, today: LocalDate): String =
    when (ChronoUnit.DAYS.between(date, today)) {
        0L -> "TODAY"
        1L -> "YESTERDAY"
        else -> "${date.dayOfMonth} ${date.month.getDisplayName(TextStyle.SHORT, Locale.US).uppercase()}"
    }
