package com.donik1998.cubeclash.feature.timer.history.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.designsystem.theme.tabular
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.Solve
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val TimeColumnWidth = 76.dp
private val HourMinute = DateTimeFormatter.ofPattern("HH:mm")

/**
 * One solve in the history list: its result on the left (penalty already folded in, DNF muted),
 * a one-line scramble preview in the middle, and the wall-clock time it was set on the right.
 *
 * The result and timestamp use tabular figures so the two number columns stay put row to row
 * instead of dancing under Noto Serif's proportional digits.
 */
@Composable
fun HistorySolveRow(
    solve: Solve,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    zoneId: ZoneId = ZoneId.systemDefault(),
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .heightIn(min = 56.dp)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = ResultFormatter.format(solve.result),
            modifier = Modifier.width(TimeColumnWidth),
            style = CubeClashTheme.typography.bodyStrong.tabular(),
            color = if (solve.isDnf) CubeClashTheme.colors.textMuted else CubeClashTheme.colors.textPrimary,
        )
        Text(
            text = solve.scramble.lines.firstOrNull()?.joinToString(" ").orEmpty(),
            modifier = Modifier.weight(1f),
            style = CubeClashTheme.typography.caption,
            color = CubeClashTheme.colors.textMuted,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        if (solve.isPb) CubeChip(label = "PB", tone = ChipTone.Brand)
        when (solve.penalty) {
            Penalty.PLUS_TWO -> CubeChip(label = "+2", tone = ChipTone.Warning)
            Penalty.DNF -> CubeChip(label = "DNF", tone = ChipTone.Danger)
            Penalty.NONE -> Unit
        }
        Text(
            text = HourMinute.format(solve.solvedAt.atZone(zoneId)),
            style = CubeClashTheme.typography.caption.tabular(),
            color = CubeClashTheme.colors.textMuted,
        )
    }
}
