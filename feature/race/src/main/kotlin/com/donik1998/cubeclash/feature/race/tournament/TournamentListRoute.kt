package com.donik1998.cubeclash.feature.race.tournament

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Layer A for the tournaments list, rendered inside the Race lobby's Tournaments tab through a slot
 * so [com.donik1998.cubeclash.feature.race.RaceLobbyScreen] stays pure. It owns its own
 * [TournamentListViewModel] — deliberately separate from the realtime `RaceViewModel` — subscribes
 * to it, and threads the open/retry callbacks. It holds no layout: everything visual lives in the
 * stateless [TournamentListScreen].
 */
@Composable
fun TournamentListRoute(
    onOpenTournament: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TournamentListViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TournamentListScreen(
        uiState = uiState,
        onOpenTournament = onOpenTournament,
        onRetry = viewModel::onRetry,
        modifier = modifier,
    )
}
