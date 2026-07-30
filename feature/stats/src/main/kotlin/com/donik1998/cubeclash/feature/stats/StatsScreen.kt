package com.donik1998.cubeclash.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.component.SegmentedControl
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.EventIcon
import com.donik1998.cubeclash.core.ui.SessionStatsRow

/**
 * Layer A: the only place that knows about state. It owns the [StatsViewModel], subscribes to it
 * and hands plain values down to the stateless [StatsScreen]. No layout, no styling lives here.
 */
@Composable
fun StatsRoute(
    onOpenPlayer: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(
        uiState = uiState,
        onSelectTab = viewModel::selectTab,
        onSelectEvent = viewModel::selectEvent,
        onSelectScope = viewModel::selectScope,
        onSelectMetric = viewModel::selectMetric,
        onRetry = viewModel::retryLeaderboard,
        onOpenPlayer = onOpenPlayer,
        modifier = modifier,
    )
}

/**
 * Layer B: the pure, testable body. Given a [StatsUiState] and callbacks it assembles the header,
 * the My Stats / Leaderboards segmented control, and either the personal stats or the
 * [LeaderboardContent]. It imports no Hilt and no ViewModel, so every state is reachable from a
 * preview with fixed data.
 */
@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onSelectTab: (StatsTab) -> Unit,
    onSelectEvent: (WcaEvent) -> Unit,
    onSelectScope: (LeaderboardScope) -> Unit,
    onSelectMetric: (LeaderboardMetric) -> Unit,
    onRetry: () -> Unit,
    onOpenPlayer: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        StatsHeader(
            event = uiState.event,
            onCycleEvent = { onSelectEvent(nextEvent(uiState.event)) },
        )

        SegmentedControl(
            options = StatsTab.entries,
            selected = uiState.tab,
            onSelect = onSelectTab,
            label = { it.label },
        )

        when (uiState.tab) {
            StatsTab.MINE -> {
                SessionStatsRow(stats = uiState.stats)
                SectionHeader("All time")
                Text(
                    text = "${uiState.stats.solveCount} solves recorded",
                    style = CubeClashTheme.typography.small,
                    color = CubeClashTheme.colors.textSecondary,
                )
            }

            StatsTab.LEADERBOARDS -> {
                SegmentedControl(
                    options = LeaderboardScope.entries,
                    selected = uiState.scope,
                    onSelect = onSelectScope,
                    label = { it.label },
                )
                LeaderboardContent(
                    state = uiState.leaderboard,
                    metric = uiState.metric,
                    event = uiState.event,
                    onSelectMetric = onSelectMetric,
                    onRetry = onRetry,
                    onOpenPlayer = onOpenPlayer,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

/**
 * Figma `hd` (44:162): the `Stats` title with the current event pinned as a right-aligned chip.
 *
 * The screen has no event-picker sheet of its own, so the chip cycles to the next event on tap —
 * enough to exercise selection while matching the single-chip layout in the design (the old
 * horizontally-scrolling event strip is not in the Leaderboards frame).
 */
@Composable
private fun StatsHeader(
    event: WcaEvent,
    onCycleEvent: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Stats",
            style = CubeClashTheme.typography.h1,
            color = CubeClashTheme.colors.textPrimary,
            modifier = Modifier.weight(1f),
        )
        CubeChip(
            label = compactEventLabel(event),
            tone = ChipTone.Brand,
            selected = true,
            onClick = onCycleEvent,
            leading = { EventIcon(event = event, active = true, size = Spacing.md) },
        )
    }
}

/** `"3×3 Cube"` → `"3×3"` — the compact form the header chip shows in Figma. */
private fun compactEventLabel(event: WcaEvent): String =
    event.displayName.removeSuffix(" Cube")

private fun nextEvent(event: WcaEvent): WcaEvent {
    val entries = WcaEvent.entries
    return entries[(event.ordinal + 1) % entries.size]
}

// --- Previews ---------------------------------------------------------------------------------

@Preview(name = "Leaderboards · Light", showBackground = true)
@Preview(name = "Leaderboards · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun StatsScreenLeaderboardsPreview() {
    CubeClashTheme {
        StatsScreen(
            uiState = StatsUiState(
                tab = StatsTab.LEADERBOARDS,
                leaderboard = LeaderboardUiState.Success(samplePage),
            ),
            onSelectTab = {},
            onSelectEvent = {},
            onSelectScope = {},
            onSelectMetric = {},
            onRetry = {},
            onOpenPlayer = {},
        )
    }
}

@Preview(name = "My Stats · Light", showBackground = true)
@Preview(name = "My Stats · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun StatsScreenMinePreview() {
    CubeClashTheme {
        StatsScreen(
            uiState = StatsUiState(tab = StatsTab.MINE),
            onSelectTab = {},
            onSelectEvent = {},
            onSelectScope = {},
            onSelectMetric = {},
            onRetry = {},
            onOpenPlayer = {},
        )
    }
}
