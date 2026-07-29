package com.donik1998.cubeclash.feature.stats

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.designsystem.component.CubeChip
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardPage
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.LeaderboardGap
import com.donik1998.cubeclash.core.ui.LeaderboardRow
import java.time.Instant

/**
 * The pure, stateless body of the Leaderboards tab — Figma frame 44:158 / 45:704 from the metric
 * chips down. It takes a resolved [LeaderboardUiState] plus the current [metric]/[event] selections
 * and emits callbacks; it imports no Hilt, no ViewModel and no DI, so every state (loading, empty,
 * error, a populated page, and the pinned viewer row) is reachable from a preview or a test without
 * booting a container.
 */
@Composable
fun LeaderboardContent(
    state: LeaderboardUiState,
    metric: LeaderboardMetric,
    event: WcaEvent,
    onSelectMetric: (LeaderboardMetric) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        MetricChipRow(selected = metric, onSelect = onSelectMetric)

        when (state) {
            is LeaderboardUiState.Loading -> LeaderboardMessage(
                text = "Loading leaderboard…",
                modifier = Modifier.fillMaxSize(),
            )

            is LeaderboardUiState.Failure -> LeaderboardError(
                message = state.message,
                onRetry = onRetry,
                modifier = Modifier.fillMaxSize(),
            )

            is LeaderboardUiState.Success ->
                if (state.isEmpty) {
                    LeaderboardMessage(
                        text = "No ranked solves yet.",
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LeaderboardList(page = state.page, event = event)
                }
        }
    }
}

/** Figma `fr` (44:179): the `single | ao5 | ao12` metric selector, active chip in brand. */
@Composable
private fun MetricChipRow(
    selected: LeaderboardMetric,
    onSelect: (LeaderboardMetric) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        LeaderboardMetric.entries.forEach { metric ->
            CubeChip(
                label = metric.label,
                selected = metric == selected,
                tone = if (metric == selected) ChipTone.Brand else ChipTone.Neutral,
                onClick = { onSelect(metric) },
            )
        }
    }
}

/**
 * Figma `lb` (44:186): the scrollable stack of ranked cards, the `· · ·` gap and the pinned
 * viewer row. The viewer sits as the final item after the gap so it reads as "you, far below".
 */
@Composable
private fun LeaderboardList(
    page: LeaderboardPage,
    event: WcaEvent,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        contentPadding = PaddingValues(bottom = Spacing.md),
    ) {
        items(page.entries, key = { it.userId }) { entry ->
            LeaderboardRow(entry = entry, event = event)
        }
        page.viewer?.let { viewer ->
            item(key = "gap") { LeaderboardGap() }
            item(key = "viewer") {
                LeaderboardRow(entry = viewer, event = event, isViewer = true)
            }
        }
    }
}

@Composable
private fun LeaderboardMessage(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(
            text = text,
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun LeaderboardError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.md, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = message ?: "Couldn't load the leaderboard.",
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        CubeSecondaryButton(
            text = "Retry",
            onClick = onRetry,
            modifier = Modifier.wrapContentSize(),
        )
    }
}

// --- Previews ---------------------------------------------------------------------------------

private fun sampleEntry(rank: Int, name: String, country: String?, ms: Long) = LeaderboardEntry(
    rank = rank,
    userId = "u$rank",
    displayName = name,
    country = country,
    valueMs = ms,
    event = WcaEvent.DEFAULT,
    solvedAt = Instant.EPOCH,
)

internal val samplePage = LeaderboardPage(
    entries = listOf(
        sampleEntry(1, "kian_r", "IR", 6_310),
        sampleEntry(2, "yush_z", "CN", 6_480),
        sampleEntry(3, "m.dubois", "FR", 6_550),
        sampleEntry(4, "s.patel", "IN", 6_710),
        sampleEntry(5, "emma_c", "US", 6_830),
    ),
    viewer = sampleEntry(1_204, "You", "UZ", 8_420),
)

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun LeaderboardContentPopulatedPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            LeaderboardContent(
                state = LeaderboardUiState.Success(samplePage),
                metric = LeaderboardMetric.SINGLE,
                event = WcaEvent.DEFAULT,
                onSelectMetric = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Loading · Light", showBackground = true)
@Preview(name = "Loading · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun LeaderboardContentLoadingPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            LeaderboardContent(
                state = LeaderboardUiState.Loading,
                metric = LeaderboardMetric.SINGLE,
                event = WcaEvent.DEFAULT,
                onSelectMetric = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Empty · Light", showBackground = true)
@Preview(name = "Empty · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun LeaderboardContentEmptyPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            LeaderboardContent(
                state = LeaderboardUiState.Success(LeaderboardPage(entries = emptyList())),
                metric = LeaderboardMetric.AO5,
                event = WcaEvent.DEFAULT,
                onSelectMetric = {},
                onRetry = {},
            )
        }
    }
}

@Preview(name = "Error · Light", showBackground = true)
@Preview(name = "Error · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun LeaderboardContentErrorPreview() {
    CubeClashTheme {
        Box(Modifier.padding(Spacing.md)) {
            LeaderboardContent(
                state = LeaderboardUiState.Failure("The network is unreachable."),
                metric = LeaderboardMetric.SINGLE,
                event = WcaEvent.DEFAULT,
                onSelectMetric = {},
                onRetry = {},
            )
        }
    }
}
