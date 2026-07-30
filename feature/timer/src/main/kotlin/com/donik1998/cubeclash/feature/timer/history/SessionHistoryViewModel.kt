package com.donik1998.cubeclash.feature.timer.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.common.asDataResult
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.stats.SessionStatsCalculator
import com.donik1998.cubeclash.core.domain.usecase.ObserveHistoryUseCase
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.WcaEvent
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.ZoneId
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * The Session & History screen's brain.
 *
 * It subscribes to the whole solve history for the current event and folds it into per-day
 * groups. The grouping and every aggregate go through [SessionStatsCalculator], so nothing here
 * formats a time or recomputes a personal best — those stay the model's job and the design
 * system's job respectively.
 */
@HiltViewModel
class SessionHistoryViewModel @Inject constructor(
    private val observeHistory: ObserveHistoryUseCase,
    private val settingsRepository: SettingsRepository,
    private val statsCalculator: SessionStatsCalculator,
) : ViewModel() {

    /**
     * The zone the day grouping is computed in. Injected as [ZoneId.systemDefault] in production;
     * a test overrides it via the secondary constructor so grouping is deterministic regardless of
     * the machine running the suite.
     */
    private var zoneId: ZoneId = ZoneId.systemDefault()

    internal constructor(
        observeHistory: ObserveHistoryUseCase,
        settingsRepository: SettingsRepository,
        statsCalculator: SessionStatsCalculator,
        zoneId: ZoneId,
    ) : this(observeHistory, settingsRepository, statsCalculator) {
        this.zoneId = zoneId
    }

    private val _uiState = MutableStateFlow<SessionHistoryUiState>(SessionHistoryUiState.Loading)
    val uiState: StateFlow<SessionHistoryUiState> = _uiState.asStateFlow()

    private var event: WcaEvent = WcaEvent.DEFAULT
    private var historyJob: Job? = null

    init {
        viewModelScope.launch {
            event = settingsRepository.settings.first().lastEvent
            observe()
        }
    }

    /** Re-subscribes from scratch — the retry the error state offers. */
    fun retry() {
        _uiState.value = SessionHistoryUiState.Loading
        observe()
    }

    private fun observe() {
        historyJob?.cancel()
        historyJob = viewModelScope.launch {
            observeHistory(event).asDataResult().collect { result ->
                _uiState.value = when (result) {
                    is DataResult.Success -> content(result.data)
                    is DataResult.Failure -> SessionHistoryUiState.Error(result.error.message)
                }
            }
        }
    }

    private fun content(solves: List<Solve>): SessionHistoryUiState.Content {
        val groups = solves
            .groupBy { it.solvedAt.atZone(zoneId).toLocalDate() }
            .toSortedMap(reverseOrder<LocalDate>())
            .map { (date, daySolves) ->
                SolveDayGroup(
                    date = date,
                    solves = daySolves,
                    stats = statsCalculator(event, daySolves),
                )
            }
        return SessionHistoryUiState.Content(
            summary = statsCalculator(event, solves),
            groups = groups,
        )
    }
}
