package com.donik1998.cubeclash.feature.timer.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.domain.timer.TimerState
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.ResultFormatter

/**
 * The giant readout.
 *
 * It **scales to its content, downwards only**: 60sp was sized against `0.00`, four glyphs,
 * while `11/13 in 54:22` is a phrase. A 3×3 still renders at exactly the design size; a long
 * result shrinks rather than clipping.
 */
@Composable
fun TimerHero(
    timerState: TimerState,
    modifier: Modifier = Modifier,
    finishedLabel: String? = null,
) {
    val colors = CubeClashTheme.colors

    val (text, stateLabel) = when (timerState) {
        is TimerState.Idle -> (finishedLabel ?: "0.00") to "Ready"
        is TimerState.Arming ->
            "0.00" to if (timerState.isReady) "Release to start" else "Hold…"

        is TimerState.Inspecting ->
            ((timerState.remainingMs / 1000).coerceAtLeast(0).toString()) to inspectionLabel(timerState)

        is TimerState.Running -> ResultFormatter.formatRunning(timerState.elapsedMs) to "Solving"
        is TimerState.Stopped -> ResultFormatter.formatRunning(timerState.elapsedMs) to stoppedLabel(timerState)
    }

    val color by animateColorAsState(
        targetValue = when {
            timerState is TimerState.Arming && timerState.isReady -> colors.success
            timerState is TimerState.Inspecting && timerState.pendingPenalty == Penalty.DNF -> colors.danger
            timerState is TimerState.Inspecting && timerState.pendingPenalty == Penalty.PLUS_TWO -> colors.warning
            else -> colors.textPrimary
        },
        label = "timerColor",
    )

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = text,
            style = CubeClashTheme.typography.display,
            color = color,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.fillMaxWidth(),
        )
        Text(
            text = stateLabel.uppercase(),
            style = CubeClashTheme.typography.overline,
            color = colors.textMuted,
        )
    }
}

private fun inspectionLabel(state: TimerState.Inspecting): String = when (state.pendingPenalty) {
    Penalty.NONE -> "Inspection"
    Penalty.PLUS_TWO -> "Inspection · +2"
    Penalty.DNF -> "Inspection · DNF"
}

private fun stoppedLabel(state: TimerState.Stopped): String = when (state.penalty) {
    Penalty.NONE -> "Solved"
    Penalty.PLUS_TWO -> "+2"
    Penalty.DNF -> "DNF"
}
