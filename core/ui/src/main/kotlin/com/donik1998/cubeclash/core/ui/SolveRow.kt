package com.donik1998.cubeclash.core.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.ResultFormatter
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.SyncState

@Composable
fun SolveRow(
    solve: Solve,
    index: Int,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        Text(
            text = "$index.",
            style = CubeClashTheme.typography.caption,
            color = CubeClashTheme.colors.textMuted,
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = ResultFormatter.format(solve.result),
                style = CubeClashTheme.typography.bodyStrong,
                color = if (solve.isDnf) CubeClashTheme.colors.textMuted else CubeClashTheme.colors.textPrimary,
            )
            Text(
                text = solve.scramble.lines.firstOrNull()?.joinToString(" ")?.take(28).orEmpty(),
                style = CubeClashTheme.typography.caption,
                color = CubeClashTheme.colors.textMuted,
                maxLines = 1,
            )
        }
        if (solve.isPb) CubeChip(label = "PB", tone = ChipTone.Brand)
        when (solve.penalty) {
            Penalty.PLUS_TWO -> CubeChip(label = "+2", tone = ChipTone.Warning)
            Penalty.DNF -> CubeChip(label = "DNF", tone = ChipTone.Danger)
            Penalty.NONE -> Unit
        }
        // Local-first means a row can legitimately exist before the server has seen it — say so.
        if (solve.syncState == SyncState.PENDING) {
            CubeChip(label = "Syncing", tone = ChipTone.Neutral)
        }
    }
}
