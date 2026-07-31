package com.donik1998.cubeclash.feature.race

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeCard
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.component.CubePrimaryButton
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.component.CubeTextField
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.component.SegmentedControl
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.RaceStatus
import com.donik1998.cubeclash.core.ui.EventIcon

@Composable
fun RaceRoute(
    onImmersiveChange: (Boolean) -> Unit,
    onOpenTournament: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: RaceViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    androidx.compose.runtime.LaunchedEffect(uiState.isInRace) { onImmersiveChange(uiState.isInRace) }

    if (uiState.room != null) {
        LiveRaceScreen(uiState = uiState, onAction = viewModel::onAction, modifier = modifier)
    } else {
        // The tournaments tab loads through its own ViewModel, kept out of the realtime RaceViewModel
        // via a slot so RaceLobbyScreen stays pure and previewable.
        RaceLobbyScreen(
            uiState = uiState,
            onAction = viewModel::onAction,
            tournamentsContent = {
                com.donik1998.cubeclash.feature.race.tournament.TournamentListRoute(
                    onOpenTournament = onOpenTournament,
                )
            },
            modifier = modifier,
        )
    }
}

@Composable
fun RaceLobbyScreen(
    uiState: RaceUiState,
    onAction: (RaceAction) -> Unit,
    modifier: Modifier = Modifier,
    tournamentsContent: @Composable () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = "Race",
            style = CubeClashTheme.typography.h1,
            color = CubeClashTheme.colors.textPrimary,
            modifier = Modifier.padding(top = Spacing.md),
        )

        SegmentedControl(
            options = LobbyTab.entries,
            selected = uiState.tab,
            onSelect = { onAction(RaceAction.SelectTab(it)) },
            label = { it.label },
        )

        when (uiState.tab) {
            LobbyTab.QUICK, LobbyTab.PRIVATE -> {
                SectionHeader("Event")
                LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                    items(uiState.eventsForTab, key = { it.id }) { event ->
                        CubeChip(
                            label = event.displayName,
                            selected = event == uiState.event,
                            onClick = { onAction(RaceAction.SelectEvent(event)) },
                            leading = { EventIcon(event = event, active = event == uiState.event, size = Spacing.md) },
                        )
                    }
                }

                if (uiState.tab == LobbyTab.QUICK) {
                    CubeCard {
                        Text(
                            text = "Quick match covers 2×2, 3×3 and One-Handed. " +
                                "Everything else raceable lives in a private room — a 6×6 queue would never fill.",
                            style = CubeClashTheme.typography.small,
                            color = CubeClashTheme.colors.textSecondary,
                        )
                    }
                    CubePrimaryButton(
                        text = "Find an opponent",
                        onClick = { onAction(RaceAction.StartMatchmaking) },
                        modifier = Modifier.fillMaxWidth(),
                    )
                } else {
                    CubeTextField(
                        value = uiState.joinCode,
                        onValueChange = { onAction(RaceAction.UpdateJoinCode(it)) },
                        label = "Room code",
                        placeholder = "CUBE42",
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
                        CubeSecondaryButton(
                            text = "Join",
                            onClick = { onAction(RaceAction.JoinPrivate) },
                            modifier = Modifier.weight(1f),
                        )
                        CubePrimaryButton(
                            text = "Create room",
                            onClick = { onAction(RaceAction.StartMatchmaking) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            // The real, tappable list, injected via a slot so this screen stays pure.
            LobbyTab.TOURNAMENTS -> tournamentsContent()
        }
    }
}

@Composable
fun LiveRaceScreen(
    uiState: RaceUiState,
    onAction: (RaceAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Back is blocked while the race is in flight: wandering off strands a real opponent.
    BackHandler(enabled = uiState.isInRace && !uiState.exitOffered) { }

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(Spacing.md),
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            CubeChip(label = statusLabel(uiState), tone = ChipTone.Danger)

            PlayerCards(uiState = uiState)

            when (uiState.status) {
                RaceStatus.WAITING -> Text(
                    text = "Looking for an opponent…",
                    style = CubeClashTheme.typography.body,
                    color = CubeClashTheme.colors.textSecondary,
                )

                RaceStatus.READY_CHECK -> CubePrimaryButton(
                    text = "I'm ready",
                    onClick = { onAction(RaceAction.Ready) },
                    modifier = Modifier.fillMaxWidth(),
                )

                RaceStatus.COUNTDOWN -> Text(
                    text = uiState.countdown?.toString() ?: "GO",
                    style = CubeClashTheme.typography.display,
                    color = CubeClashTheme.colors.brandPrimary,
                )

                RaceStatus.RACING -> {
                    com.donik1998.cubeclash.core.ui.ScrambleCard(scramble = uiState.scramble)
                    CubePrimaryButton(
                        text = "Stop",
                        onClick = { onAction(RaceAction.StopSolve) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                    )
                }

                RaceStatus.SETTLED -> ResultPanel(uiState = uiState, onAction = onAction)
            }

            if (uiState.exitOffered && uiState.status != RaceStatus.SETTLED) {
                CubeCard {
                    Text(
                        text = "Nothing from the server for a while. Your time is already submitted — " +
                            "leaving won't forfeit the race.",
                        style = CubeClashTheme.typography.small,
                        color = CubeClashTheme.colors.textSecondary,
                    )
                    CubeSecondaryButton(text = "Leave", onClick = { onAction(RaceAction.LeaveRace) })
                }
            }
        }
    }
}

@Composable
private fun PlayerCards(uiState: RaceUiState) {
    val room = uiState.room ?: return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        room.players.take(2).forEachIndexed { index, player ->
            CubeCard(modifier = Modifier.weight(1f)) {
                Text(
                    text = player.displayName,
                    style = CubeClashTheme.typography.title,
                    color = CubeClashTheme.colors.textPrimary,
                )
                Text(
                    text = player.country.orEmpty(),
                    style = CubeClashTheme.typography.caption,
                    color = CubeClashTheme.colors.textMuted,
                )
                Text(
                    // No hero timer and no progress bars: sizing your own clock larger turns a
                    // race back into a solo solve with a footnote.
                    text = com.donik1998.cubeclash.core.model.ResultFormatter.formatRunning(
                        if (index == 0) uiState.yourRunningMs else uiState.opponentRunningMs,
                    ),
                    style = CubeClashTheme.typography.h2,
                    color = CubeClashTheme.colors.textPrimary,
                )
            }
            if (index == 0 && room.players.size > 1) {
                Text(
                    text = "VS",
                    style = CubeClashTheme.typography.overline,
                    color = CubeClashTheme.colors.textMuted,
                )
            }
        }
    }
}

@Composable
private fun ResultPanel(uiState: RaceUiState, onAction: (RaceAction) -> Unit) {
    val result = uiState.result ?: return
    val won = result.winnerUserId == "me"
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        Text(
            text = if (won) "You win" else "You lose",
            style = CubeClashTheme.typography.h1,
            color = if (won) CubeClashTheme.colors.success else CubeClashTheme.colors.danger,
        )
        result.eloDelta?.let {
            Text(
                text = if (it > 0) "+$it Elo" else "$it Elo",
                style = CubeClashTheme.typography.label,
                color = CubeClashTheme.colors.textSecondary,
            )
        }
        CubePrimaryButton(text = "Back to lobby", onClick = { onAction(RaceAction.LeaveRace) })
    }
}

private fun statusLabel(uiState: RaceUiState): String = when (uiState.status) {
    RaceStatus.WAITING -> "● MATCHING"
    RaceStatus.READY_CHECK -> "● READY CHECK"
    RaceStatus.COUNTDOWN -> "● STARTING"
    RaceStatus.RACING -> "● LIVE RACE"
    RaceStatus.SETTLED -> "● FINISHED"
}
