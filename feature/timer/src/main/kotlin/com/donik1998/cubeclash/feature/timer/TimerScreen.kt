package com.donik1998.cubeclash.feature.timer

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.component.CubeGhostButton
import com.donik1998.cubeclash.core.designsystem.component.SegmentedControl
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.ui.EventIcon
import com.donik1998.cubeclash.core.ui.ScrambleCard
import com.donik1998.cubeclash.core.ui.SessionStatsRow
import com.donik1998.cubeclash.feature.timer.component.EventPickerSheet
import com.donik1998.cubeclash.feature.timer.component.TimerHero

@Composable
fun TimerRoute(
    onImmersiveChange: (Boolean) -> Unit,
    onOpenHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Hiding the nav bar is a side effect, not part of drawing this frame.
    LaunchedEffect(uiState.isImmersive) { onImmersiveChange(uiState.isImmersive) }

    TimerScreen(
        uiState = uiState,
        onAction = viewModel::onAction,
        onOpenHistory = onOpenHistory,
        modifier = modifier,
    )
}

@Composable
fun TimerScreen(
    uiState: TimerUiState,
    onAction: (TimerAction) -> Unit,
    onOpenHistory: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            // The whole surface is the timer. Press and release are separate signals because
            // the difference between them is what a hold-style start is made of.
            .pointerInput(uiState.timerStyle) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    onAction(TimerAction.PointerDown)
                    waitForUpOrCancellation()
                    onAction(TimerAction.PointerUp)
                }
            },
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            if (!uiState.isImmersive) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = Spacing.md),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CubeChip(
                        label = uiState.event.displayName,
                        tone = ChipTone.Brand,
                        onClick = { onAction(TimerAction.OpenEventPicker) },
                        leading = { EventIcon(event = uiState.event, active = true, size = 16.dp) },
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CubeGhostButton(text = "History", onClick = onOpenHistory)
                        CubeGhostButton(text = "New", onClick = { onAction(TimerAction.NewScramble) })
                    }
                }

                SegmentedControl(
                    options = ScrambleSource.entries,
                    selected = uiState.scrambleSource,
                    onSelect = { onAction(TimerAction.SelectScrambleSource(it)) },
                    label = { it.label },
                )

                ScrambleCard(scramble = uiState.scramble)
            }

            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                TimerHero(timerState = uiState.timerState)
            }

            if (uiState.showPenaltyControls) {
                Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    if (uiState.event.supportsPlusTwo) {
                        CubeChip(
                            label = "+2",
                            tone = ChipTone.Warning,
                            selected = uiState.pendingPenalty == Penalty.PLUS_TWO,
                            onClick = { onAction(TimerAction.ApplyPenalty(Penalty.PLUS_TWO)) },
                        )
                    }
                    CubeChip(
                        label = "DNF",
                        tone = ChipTone.Danger,
                        selected = uiState.pendingPenalty == Penalty.DNF,
                        onClick = { onAction(TimerAction.ApplyPenalty(Penalty.DNF)) },
                    )
                    CubeChip(
                        label = "Clear",
                        selected = uiState.pendingPenalty == Penalty.NONE,
                        onClick = { onAction(TimerAction.ApplyPenalty(Penalty.NONE)) },
                    )
                }
            }

            if (!uiState.isImmersive) {
                SessionStatsRow(
                    stats = uiState.stats,
                    modifier = Modifier.padding(bottom = Spacing.md),
                )
            }
        }
    }

    if (uiState.isEventPickerOpen) {
        EventPickerSheet(
            selected = uiState.event,
            onSelect = { onAction(TimerAction.SelectEvent(it)) },
            onDismiss = { onAction(TimerAction.DismissEventPicker) },
        )
    }
}

@Preview
@Composable
private fun TimerScreenPreview() {
    CubeClashTheme {
        TimerScreen(uiState = TimerUiState(), onAction = {})
    }
}
