package com.donik1998.cubeclash.feature.timer.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.ui.EmptyState
import com.donik1998.cubeclash.feature.timer.history.component.HistoryDayHeader
import com.donik1998.cubeclash.feature.timer.history.component.HistorySolveRow
import com.donik1998.cubeclash.feature.timer.history.component.HistorySummaryRow
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * Layer A: the only place that knows about state. It owns the [SessionHistoryViewModel], collects
 * its stream and forwards navigation up. No layout or styling lives here.
 */
@Composable
fun SessionHistoryRoute(
    onBack: () -> Unit,
    onOpenSolve: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SessionHistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    SessionHistoryScreen(
        uiState = uiState,
        onBack = onBack,
        onOpenSolve = onOpenSolve,
        onRetry = viewModel::retry,
        modifier = modifier,
    )
}

/**
 * Layer B: pure and testable. Given a resolved [SessionHistoryUiState] and callbacks it draws the
 * scaffold, the summary cards, and every day group. It imports no Hilt and no ViewModel, so
 * loading, error, empty and populated are all reachable from a preview with fixed data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SessionHistoryScreen(
    uiState: SessionHistoryUiState,
    onBack: () -> Unit,
    onOpenSolve: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = CubeClashTheme.colors.canvas,
        topBar = {
            TopAppBar(
                title = { Text("History", style = CubeClashTheme.typography.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = CubeClashTheme.colors.canvas,
                    titleContentColor = CubeClashTheme.colors.textPrimary,
                    navigationIconContentColor = CubeClashTheme.colors.textPrimary,
                ),
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
        ) {
            when (uiState) {
                is SessionHistoryUiState.Loading -> LoadingBody()
                is SessionHistoryUiState.Error -> ErrorBody(message = uiState.message, onRetry = onRetry)
                is SessionHistoryUiState.Content ->
                    if (uiState.isEmpty) {
                        EmptyBody()
                    } else {
                        HistoryList(content = uiState, onOpenSolve = onOpenSolve)
                    }
            }
        }
    }
}

@Composable
private fun HistoryList(
    content: SessionHistoryUiState.Content,
    onOpenSolve: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = LocalDate.now()
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = Spacing.md),
    ) {
        item(key = "summary") {
            HistorySummaryRow(
                stats = content.summary,
                modifier = Modifier.padding(horizontal = Spacing.md, vertical = Spacing.xs),
            )
        }
        content.groups.forEach { group ->
            item(key = "header-${group.date}") {
                HistoryDayHeader(
                    date = group.date,
                    count = group.count,
                    best = group.best,
                    today = today,
                    modifier = Modifier.padding(
                        horizontal = Spacing.md,
                        vertical = Spacing.sm,
                    ),
                )
            }
            items(group.solves, key = { it.id }) { solve ->
                HistorySolveRow(solve = solve, onClick = { onOpenSolve(solve.id) })
            }
        }
    }
}

@Composable
private fun LoadingBody() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = CubeClashTheme.colors.brandPrimary)
    }
}

@Composable
private fun EmptyBody() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        EmptyState(
            title = "No solves yet",
            message = "Every solve you time will show up here.",
        )
    }
}

@Composable
private fun ErrorBody(message: String?, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.foundation.layout.Column(
            modifier = Modifier.padding(Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = message ?: "Couldn't load your history.",
                style = CubeClashTheme.typography.body,
                color = CubeClashTheme.colors.textSecondary,
            )
            CubeSecondaryButton(text = "Retry", onClick = onRetry)
        }
    }
}

// --- Previews ---------------------------------------------------------------------------------

private fun previewSolve(
    id: String,
    timeMs: Long,
    daysAgo: Long,
    hour: Int,
    penalty: Penalty = Penalty.NONE,
    isPb: Boolean = false,
    scramble: String = "R U R' U' F R F'",
) = Solve(
    id = id,
    clientId = id,
    event = WcaEvent.THREE,
    scramble = Scramble.parse(scramble, ScrambleNotation.FACE_TURN),
    scrambleSource = ScrambleSource.RANDOM,
    timeMs = timeMs,
    penalty = penalty,
    solvedAt = LocalDate.now().minusDays(daysAgo).atTime(hour, 30).toInstant(ZoneOffset.UTC),
    isPb = isPb,
)

private val previewContent = SessionHistoryUiState.Content(
    summary = EventStats(WcaEvent.THREE, best = 7_310, ao5 = 8_120, ao12 = 8_640),
    groups = listOf(
        SolveDayGroup(
            date = LocalDate.now(),
            solves = listOf(
                previewSolve("1", 7_310, 0, 14, isPb = true),
                previewSolve("2", 9_040, 0, 13, penalty = Penalty.PLUS_TWO),
                previewSolve("3", 12_500, 0, 12, penalty = Penalty.DNF),
            ),
            stats = EventStats(WcaEvent.THREE, best = 7_310, solveCount = 3),
        ),
        SolveDayGroup(
            date = LocalDate.now().minusDays(1),
            solves = listOf(previewSolve("4", 8_820, 1, 20)),
            stats = EventStats(WcaEvent.THREE, best = 8_820, solveCount = 1),
        ),
    ),
)

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SessionHistoryPopulatedPreview() {
    CubeClashTheme {
        SessionHistoryScreen(previewContent, onBack = {}, onOpenSolve = {}, onRetry = {})
    }
}

@Preview(name = "Empty · Light", showBackground = true)
@Preview(name = "Empty · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SessionHistoryEmptyPreview() {
    CubeClashTheme {
        SessionHistoryScreen(
            SessionHistoryUiState.Content(EventStats(WcaEvent.THREE), emptyList()),
            onBack = {}, onOpenSolve = {}, onRetry = {},
        )
    }
}

@Preview(name = "Loading · Light", showBackground = true)
@Preview(name = "Loading · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SessionHistoryLoadingPreview() {
    CubeClashTheme {
        SessionHistoryScreen(SessionHistoryUiState.Loading, onBack = {}, onOpenSolve = {}, onRetry = {})
    }
}

@Preview(name = "Error · Light", showBackground = true)
@Preview(name = "Error · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun SessionHistoryErrorPreview() {
    CubeClashTheme {
        SessionHistoryScreen(
            SessionHistoryUiState.Error("Can't reach CubeClash right now."),
            onBack = {}, onOpenSolve = {}, onRetry = {},
        )
    }
}
