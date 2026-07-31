package com.donik1998.cubeclash.feature.race.tournament

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.TournamentRepository
import com.donik1998.cubeclash.core.model.TournamentDetail
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * A single tournament — header plus bracket — and the register action. [Success] carries the domain
 * [TournamentDetail] directly; [Failure] carries a renderable message plus a retry, a real path
 * today because `GET /tournaments/{id}` 404s on the live server. [registerError] is a transient
 * banner overlaid on [Success] when a register attempt is rejected (e.g. the bracket filled up
 * between load and tap), without tearing down the loaded detail.
 */
sealed interface TournamentDetailUiState {
    data object Loading : TournamentDetailUiState
    data class Success(
        val detail: TournamentDetail,
        val registering: Boolean = false,
        val registerError: String? = null,
    ) : TournamentDetailUiState
    data class Failure(val message: String?) : TournamentDetailUiState
}

/**
 * Layer A's brains for the detail screen: loads the bracket for the [tournamentId] carried in the
 * back stack (read from [SavedStateHandle] under the destination's property name, the same
 * type-safe-nav convention the stats screens use) and owns the register flow — a successful register
 * re-fetches so the entrant count and the registered flag reflect the mutation.
 */
@HiltViewModel
class TournamentDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val tournamentRepository: TournamentRepository,
) : ViewModel() {

    private val tournamentId: String = requireNotNull(savedStateHandle[TOURNAMENT_ID_KEY]) {
        "TournamentDetailDestination requires a tournamentId argument"
    }

    private val _uiState = MutableStateFlow<TournamentDetailUiState>(TournamentDetailUiState.Loading)
    val uiState: StateFlow<TournamentDetailUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    fun onRegister() {
        val current = _uiState.value
        if (current !is TournamentDetailUiState.Success || current.registering) return
        viewModelScope.launch {
            _uiState.value = current.copy(registering = true, registerError = null)
            when (val result = tournamentRepository.register(tournamentId)) {
                is DataResult.Success -> refresh()
                is DataResult.Failure -> {
                    // Keep the loaded detail; surface the rejection as a banner.
                    val latest = _uiState.value
                    if (latest is TournamentDetailUiState.Success) {
                        _uiState.value = latest.copy(
                            registering = false,
                            registerError = result.error.message,
                        )
                    }
                }
            }
        }
    }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = TournamentDetailUiState.Loading
            _uiState.value = resolve()
        }
    }

    /** Re-fetch after a successful register, replacing the header/bracket in place. */
    private suspend fun refresh() {
        _uiState.value = resolve()
    }

    private suspend fun resolve(): TournamentDetailUiState =
        when (val result = tournamentRepository.tournament(tournamentId)) {
            is DataResult.Success -> TournamentDetailUiState.Success(result.data)
            is DataResult.Failure -> TournamentDetailUiState.Failure(result.error.message)
        }

    companion object {
        /** Type-safe nav stores the destination's `tournamentId` property under this key. */
        const val TOURNAMENT_ID_KEY = "tournamentId"
    }
}
