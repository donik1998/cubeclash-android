package com.donik1998.cubeclash.feature.race.tournament

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.donik1998.cubeclash.core.designsystem.component.CubeSecondaryButton
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.core.designsystem.theme.Radius
import com.donik1998.cubeclash.core.designsystem.theme.Spacing
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.core.ui.EmptyState
import com.donik1998.cubeclash.feature.race.tournament.component.TournamentRow
import com.donik1998.cubeclash.feature.race.tournament.component.sampleTournament

/**
 * Layer B: the pure, testable body of the tournaments list — an exhaustive `when` over the three
 * states (a skeleton while loading, a "Couldn't load tournaments" error with a retry, or the list /
 * an empty state). It imports no Hilt and no ViewModel, so every state — including the real error
 * arm (`GET /tournaments` 404s today) — is reachable from a preview or a test with fixed data.
 */
@Composable
fun TournamentListScreen(
    uiState: TournamentListUiState,
    onOpenTournament: (String) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    when (uiState) {
        is TournamentListUiState.Loading -> TournamentListSkeleton(modifier)

        is TournamentListUiState.Failure -> TournamentListError(
            message = uiState.message,
            onRetry = onRetry,
            modifier = modifier.fillMaxSize(),
        )

        is TournamentListUiState.Success -> {
            if (uiState.tournaments.isEmpty()) {
                EmptyState(
                    title = "No tournaments yet",
                    message = "There are no brackets open right now. Check back — scheduled comps and a " +
                        "Global Weekly are on the way.",
                    modifier = modifier,
                )
            } else {
                LazyColumn(
                    modifier = modifier.fillMaxSize(),
                    contentPadding = PaddingValues(bottom = Spacing.md),
                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    items(uiState.tournaments, key = { it.id }) { tournament ->
                        TournamentRow(
                            tournament = tournament,
                            onClick = { onOpenTournament(tournament.id) },
                        )
                    }
                }
            }
        }
    }
}

/** The loading placeholder: four card-shaped blocks, matching the row height. */
@Composable
private fun TournamentListSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(Spacing.sm),
    ) {
        repeat(4) { SkeletonBlock(height = 96.dp) }
    }
}

@Composable
private fun SkeletonBlock(height: Dp, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
            .background(CubeClashTheme.colors.surfaceAlt, Radius.card),
    )
}

/**
 * The failure arm. `GET /tournaments` 404s on the live server today, so this is a state a real
 * device reaches; the upstream message sits under the title so a network blip still reads as one.
 */
@Composable
private fun TournamentListError(
    message: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(Spacing.sm, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Couldn't load tournaments",
            style = CubeClashTheme.typography.h2,
            color = CubeClashTheme.colors.textPrimary,
            textAlign = TextAlign.Center,
        )
        Text(
            text = message ?: "We couldn't reach the tournament lobby.",
            style = CubeClashTheme.typography.body,
            color = CubeClashTheme.colors.textSecondary,
            textAlign = TextAlign.Center,
        )
        CubeSecondaryButton(text = "Retry", onClick = onRetry)
    }
}

// --- Previews ---------------------------------------------------------------------------------

private fun sampleTournaments() = listOf(
    sampleTournament(
        id = "weekly-333",
        name = "Global Weekly · 3×3",
        status = TournamentStatus.LIVE,
        entrants = 64,
        capacity = 64,
        startsAt = null,
    ),
    sampleTournament(),
    sampleTournament(
        id = "oh",
        name = "One-Handed Invitational",
        status = TournamentStatus.UPCOMING,
        entrants = 12,
        capacity = 16,
        registered = true,
    ),
    sampleTournament(
        id = "blitz",
        name = "2×2 Blitz",
        status = TournamentStatus.FINISHED,
        entrants = 32,
        capacity = 32,
    ),
)

@Preview(name = "Populated · Light", showBackground = true)
@Preview(name = "Populated · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentListPopulatedPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas).padding(Spacing.md)) {
            TournamentListScreen(TournamentListUiState.Success(sampleTournaments()), {}, {})
        }
    }
}

@Preview(name = "Empty · Light", showBackground = true)
@Preview(name = "Empty · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentListEmptyPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas).padding(Spacing.md)) {
            TournamentListScreen(TournamentListUiState.Success(emptyList()), {}, {})
        }
    }
}

@Preview(name = "Loading · Light", showBackground = true)
@Preview(name = "Loading · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentListLoadingPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas).padding(Spacing.md)) {
            TournamentListScreen(TournamentListUiState.Loading, {}, {})
        }
    }
}

@Preview(name = "Error · Light", showBackground = true)
@Preview(name = "Error · Dark", showBackground = true, uiMode = 0x20)
@Composable
private fun TournamentListErrorPreview() {
    CubeClashTheme {
        Box(Modifier.background(CubeClashTheme.colors.canvas).padding(Spacing.md)) {
            TournamentListScreen(
                TournamentListUiState.Failure("The tournament service is unavailable (404)."),
                {}, {},
            )
        }
    }
}
