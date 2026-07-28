package com.donik1998.cubeclash.feature.stats

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.component.SectionHeader
import com.donik1998.cubeclash.core.designsystem.component.SegmentedControl
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.EventIcon
import com.donik1998.cubeclash.core.ui.LeaderboardRow
import com.donik1998.cubeclash.core.ui.SessionStatsRow

@Composable
fun StatsRoute(
    modifier: Modifier = Modifier,
    viewModel: StatsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    StatsScreen(
        uiState = uiState,
        onSelectTab = viewModel::selectTab,
        onSelectEvent = viewModel::selectEvent,
        onSelectScope = viewModel::selectScope,
        modifier = modifier,
    )
}

@Composable
fun StatsScreen(
    uiState: StatsUiState,
    onSelectTab: (StatsTab) -> Unit,
    onSelectEvent: (WcaEvent) -> Unit,
    onSelectScope: (LeaderboardScope) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = Spacing.md),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = "Stats",
            style = CubeClashTheme.typography.h1,
            color = CubeClashTheme.colors.textPrimary,
            modifier = Modifier.padding(top = Spacing.md),
        )

        SegmentedControl(
            options = StatsTab.entries,
            selected = uiState.tab,
            onSelect = onSelectTab,
            label = { it.label },
        )

        LazyRow(horizontalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            items(WcaEvent.entries, key = { it.id }) { event ->
                CubeChip(
                    label = event.displayName,
                    selected = event == uiState.event,
                    onClick = { onSelectEvent(event) },
                    leading = { EventIcon(event = event, active = event == uiState.event, size = Spacing.md) },
                )
            }
        }

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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(Spacing.xxs)) {
                    items(uiState.leaderboard, key = { it.user.id }) { entry ->
                        LeaderboardRow(entry = entry, event = uiState.event)
                    }
                }
            }
        }
    }
}
