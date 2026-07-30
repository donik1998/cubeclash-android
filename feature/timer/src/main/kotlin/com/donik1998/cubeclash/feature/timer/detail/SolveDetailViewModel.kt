package com.donik1998.cubeclash.feature.timer.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.usecase.DeleteSolveUseCase
import com.donik1998.cubeclash.core.domain.usecase.ObserveHistoryUseCase
import com.donik1998.cubeclash.core.domain.usecase.UpdatePenaltyUseCase
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.WcaEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Solve Detail's brain.
 *
 * There is no `GET /solves/:id`, so the solve is resolved by watching the history flow and picking
 * the row with the matching id. Because that flow is local-first and live, an edit made here — a
 * penalty change — flows straight back in and updates the readout without a manual refresh.
 */
@HiltViewModel
class SolveDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val observeHistory: ObserveHistoryUseCase,
    private val updatePenalty: UpdatePenaltyUseCase,
    private val deleteSolve: DeleteSolveUseCase,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val solveId: String = requireNotNull(savedStateHandle[SOLVE_ID_KEY]) {
        "SolveDetail launched without a solve id"
    }

    private val _uiState = MutableStateFlow(SolveDetailUiState(solveId = solveId))
    val uiState: StateFlow<SolveDetailUiState> = _uiState.asStateFlow()

    private var event: WcaEvent = WcaEvent.DEFAULT
    private var observeJob: Job? = null

    init {
        viewModelScope.launch {
            event = settingsRepository.settings.first().lastEvent
            observe()
        }
    }

    private fun observe() {
        observeJob?.cancel()
        observeJob = viewModelScope.launch {
            observeHistory(event).collect { solves ->
                val match = solves.firstOrNull { it.id == solveId }
                _uiState.update { state ->
                    // A delete-in-progress must not be undone by a stale emission that still
                    // carries the row; once it's gone from the flow, it stays gone.
                    if (state.isDeleted) state
                    else state.copy(solve = match, isLoading = false)
                }
            }
        }
    }

    fun changePenalty(penalty: Penalty) {
        val current = _uiState.value.solve ?: return
        if (current.penalty == penalty) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = updatePenalty(solveId, penalty)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(solve = result.data, isSaving = false) }
                is DataResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, message = result.error.message) }
            }
        }
    }

    fun delete() {
        if (_uiState.value.solve == null) return
        _uiState.update { it.copy(isSaving = true) }
        viewModelScope.launch {
            when (val result = deleteSolve(solveId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(isSaving = false, isDeleted = true) }
                is DataResult.Failure ->
                    _uiState.update { it.copy(isSaving = false, message = result.error.message) }
            }
        }
    }

    fun dismissMessage() = _uiState.update { it.copy(message = null) }

    companion object {
        /** Type-safe nav stores the destination's `solveId` property under this key. */
        const val SOLVE_ID_KEY = "solveId"
    }
}
