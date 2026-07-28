package com.donik1998.cubeclash.feature.timer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.common.TimeSource
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.timer.TimerConfig
import com.donik1998.cubeclash.core.domain.timer.TimerEvent
import com.donik1998.cubeclash.core.domain.timer.TimerState
import com.donik1998.cubeclash.core.domain.timer.TimerStateMachine
import com.donik1998.cubeclash.core.domain.usecase.LogSolveUseCase
import com.donik1998.cubeclash.core.domain.usecase.NextScrambleUseCase
import com.donik1998.cubeclash.core.domain.usecase.ObserveSessionStatsUseCase
import com.donik1998.cubeclash.core.domain.usecase.ObserveSessionUseCase
import com.donik1998.cubeclash.core.domain.usecase.UpdatePenaltyUseCase
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.WcaEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The timer screen's brain.
 *
 * All the interesting logic — when a hold becomes a start, what the inspection overrun costs —
 * lives in [TimerStateMachine], which is pure and clock-injected. This class only supplies the
 * clock, pumps ticks while something is actually counting, and writes the solve down when the
 * machine says it stopped.
 */
@HiltViewModel
class TimerViewModel @Inject constructor(
    private val observeSession: ObserveSessionUseCase,
    private val observeStats: ObserveSessionStatsUseCase,
    private val nextScramble: NextScrambleUseCase,
    private val logSolve: LogSolveUseCase,
    private val updatePenalty: UpdatePenaltyUseCase,
    private val settingsRepository: SettingsRepository,
    private val timeSource: TimeSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimerUiState())
    val uiState: StateFlow<TimerUiState> = _uiState.asStateFlow()

    private var tickJob: Job? = null
    private var sessionJob: Job? = null

    init {
        viewModelScope.launch {
            val settings = settingsRepository.settings.first()
            _uiState.update {
                it.copy(
                    event = settings.lastEvent,
                    timerStyle = settings.timerStyle,
                    inspectionEnabled = settings.inspectionEnabled,
                )
            }
            observe(settings.lastEvent)
            rollScramble(settings.lastEvent)
        }

        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                _uiState.update {
                    it.copy(timerStyle = settings.timerStyle, inspectionEnabled = settings.inspectionEnabled)
                }
            }
        }
    }

    fun onAction(action: TimerAction) {
        when (action) {
            TimerAction.PointerDown -> dispatch(TimerEvent.PointerDown(timeSource.nowMillis()))
            TimerAction.PointerUp -> dispatch(TimerEvent.PointerUp(timeSource.nowMillis()))
            TimerAction.NewScramble -> viewModelScope.launch { rollScramble(_uiState.value.event) }
            is TimerAction.SelectEvent -> selectEvent(action.event)
            is TimerAction.SelectScrambleSource -> selectSource(action.source)
            is TimerAction.ApplyPenalty -> applyPenalty(action.penalty)
            TimerAction.DeleteLastSolve -> Unit
            TimerAction.OpenEventPicker -> _uiState.update { it.copy(isEventPickerOpen = true) }
            TimerAction.DismissEventPicker -> _uiState.update { it.copy(isEventPickerOpen = false) }
            TimerAction.DismissMessage -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun dispatch(event: TimerEvent) {
        val current = _uiState.value
        val config = TimerConfig(current.timerStyle, current.inspectionEnabled)
        val next = TimerStateMachine.reduce(current.timerState, event, config)
        if (next == current.timerState) return

        _uiState.update { it.copy(timerState = next) }

        // Ticks are only pumped while something is counting, so an idle timer costs nothing.
        if (next.needsTicks()) startTicking() else stopTicking()

        if (next is TimerState.Stopped) recordSolve(next)
    }

    private fun TimerState.needsTicks(): Boolean =
        this is TimerState.Arming || this is TimerState.Inspecting || this is TimerState.Running

    private fun startTicking() {
        if (tickJob?.isActive == true) return
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(TICK_MS)
                dispatch(TimerEvent.Tick(timeSource.nowMillis()))
            }
        }
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    private fun recordSolve(stopped: TimerState.Stopped) {
        val state = _uiState.value
        viewModelScope.launch {
            val result = logSolve(
                event = state.event,
                scramble = state.scramble,
                scrambleSource = state.scrambleSource,
                timeMs = stopped.elapsedMs,
                penalty = stopped.penalty,
            )
            when (result) {
                is DataResult.Success -> _uiState.update { it.copy(lastSolve = result.data) }
                is DataResult.Failure -> _uiState.update { it.copy(message = result.error.message) }
            }
            // The next scramble is proposed immediately — the timer always offers one.
            rollScramble(state.event)
        }
    }

    private fun applyPenalty(penalty: Penalty) {
        val solve = _uiState.value.lastSolve ?: return
        viewModelScope.launch {
            when (val result = updatePenalty(solve.id, penalty)) {
                is DataResult.Success -> _uiState.update { it.copy(lastSolve = result.data) }
                is DataResult.Failure -> _uiState.update { it.copy(message = result.error.message) }
            }
        }
    }

    private fun selectEvent(event: WcaEvent) {
        _uiState.update {
            it.copy(event = event, isEventPickerOpen = false, timerState = TimerState.Idle, lastSolve = null)
        }
        viewModelScope.launch {
            settingsRepository.setLastEvent(event)
            observe(event)
            rollScramble(event)
        }
    }

    private fun selectSource(source: ScrambleSource) {
        _uiState.update { it.copy(scrambleSource = source) }
        if (source == ScrambleSource.WCA_COMPS) {
            // Says what it can't do instead of quietly serving something else.
            _uiState.update { it.copy(message = "Competition scrambles aren't wired up yet.") }
        }
    }

    private fun observe(event: WcaEvent) {
        sessionJob?.cancel()
        sessionJob = viewModelScope.launch {
            launch { observeSession(event).collect { session -> _uiState.update { it.copy(session = session) } } }
            launch { observeStats(event).collect { stats -> _uiState.update { it.copy(stats = stats) } } }
        }
    }

    private suspend fun rollScramble(event: WcaEvent) {
        when (val result = nextScramble(event)) {
            is DataResult.Success -> _uiState.update { it.copy(scramble = result.data) }
            is DataResult.Failure -> _uiState.update { it.copy(message = result.error.message) }
        }
    }

    override fun onCleared() {
        stopTicking()
        super.onCleared()
    }

    private companion object {
        /** ~60 Hz. Fast enough that the hundredths never look like they are skipping. */
        const val TICK_MS = 16L
    }
}
