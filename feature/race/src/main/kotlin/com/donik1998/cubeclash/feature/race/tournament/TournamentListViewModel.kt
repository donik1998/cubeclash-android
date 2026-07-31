package com.donik1998.cubeclash.feature.race.tournament

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.TournamentRepository
import com.donik1998.cubeclash.core.model.Tournament
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The Tournaments-tab lobby list. [Success] carries the domain [Tournament] list directly and
 * [Failure] carries a renderable message plus a retry — real today, because `GET /tournaments` 404s
 * on the live server, so the error arm is a path a real device hits, not a hypothetical.
 */
sealed interface TournamentListUiState {
    data object Loading : TournamentListUiState
    data class Success(val tournaments: List<Tournament>) : TournamentListUiState
    data class Failure(val message: String?) : TournamentListUiState
}

/**
 * Layer A's brains for the tournaments list. Lives alongside — but deliberately separate from — the
 * realtime [RaceViewModel]: this owns only the lobby listing and never touches the race gateway, so
 * the tournaments tab can load and fail on its own without entangling the live-race state machine.
 */
@HiltViewModel
class TournamentListViewModel @Inject constructor(
    private val tournamentRepository: TournamentRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow<TournamentListUiState>(TournamentListUiState.Loading)
    val uiState: StateFlow<TournamentListUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun onRetry() = load()

    private fun load() {
        viewModelScope.launch {
            _uiState.value = TournamentListUiState.Loading
            _uiState.value = when (val result = tournamentRepository.tournaments()) {
                is DataResult.Success -> TournamentListUiState.Success(result.data)
                is DataResult.Failure -> TournamentListUiState.Failure(result.error.message)
            }
        }
    }
}
