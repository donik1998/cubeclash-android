package com.donik1998.cubeclash.feature.race.tournament

import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

/**
 * Layer A: the only place that knows about state. It owns the [TournamentDetailViewModel] (which
 * reads the tapped `tournamentId` from the back stack), subscribes to it, wires retry and register,
 * and pops itself on back. It holds no layout — everything visual lives in the stateless
 * [TournamentDetailScreen].
 */
@Composable
fun TournamentDetailRoute(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TournamentDetailViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    BackHandler(onBack = onBack)

    TournamentDetailScreen(
        uiState = uiState,
        onBack = onBack,
        onRetry = viewModel::onRetry,
        onRegister = viewModel::onRegister,
        modifier = modifier,
    )
}
