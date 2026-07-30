package com.donik1998.cubeclash.feature.timer.detail.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.WcaEvent

/**
 * The none / +2 / DNF selector, the same three chips the timer shows after a solve. `+2` is
 * hidden for events that don't take a time penalty (Fewest Moves, Multi-Blind), matching the
 * timer's own rule rather than offering a control that does nothing.
 */
@Composable
fun PenaltyControls(
    event: WcaEvent,
    selected: Penalty,
    onSelect: (Penalty) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        CubeChip(
            label = "None",
            selected = selected == Penalty.NONE,
            onClick = if (enabled) ({ onSelect(Penalty.NONE) }) else null,
        )
        if (event.supportsPlusTwo) {
            CubeChip(
                label = "+2",
                tone = ChipTone.Warning,
                selected = selected == Penalty.PLUS_TWO,
                onClick = if (enabled) ({ onSelect(Penalty.PLUS_TWO) }) else null,
            )
        }
        CubeChip(
            label = "DNF",
            tone = ChipTone.Danger,
            selected = selected == Penalty.DNF,
            onClick = if (enabled) ({ onSelect(Penalty.DNF) }) else null,
        )
    }
}
