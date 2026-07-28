package com.donik1998.cubeclash.feature.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.SettingsRepository
import com.donik1998.cubeclash.core.domain.repository.StatsRepository
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardScope
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

enum class StatsTab(val label: String) { MINE("My Stats"), LEADERBOARDS("Leaderboards") }

data class StatsUiState(
    val tab: StatsTab = StatsTab.MINE,
    val event: WcaEvent = WcaEvent.DEFAULT,
    val stats: EventStats = EventStats(WcaEvent.DEFAULT),
    val scope: LeaderboardScope = LeaderboardScope.GLOBAL,
    val metric: LeaderboardMetric = LeaderboardMetric.SINGLE,
    val leaderboard: List<LeaderboardEntry> = emptyList(),
    val isLoading: Boolean = false,
    val message: String? = null,
)

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val statsRepository: StatsRepository,
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()

    private var statsJob: Job? = null

    init {
        viewModelScope.launch {
            val event = settingsRepository.settings.first().lastEvent
            _uiState.update { it.copy(event = event) }
            observe(event)
            loadLeaderboard()
        }
    }

    fun selectTab(tab: StatsTab) {
        _uiState.update { it.copy(tab = tab) }
        if (tab == StatsTab.LEADERBOARDS) loadLeaderboard()
    }

    fun selectEvent(event: WcaEvent) {
        _uiState.update { it.copy(event = event) }
        observe(event)
        loadLeaderboard()
    }

    fun selectScope(scope: LeaderboardScope) {
        _uiState.update { it.copy(scope = scope) }
        loadLeaderboard()
    }

    fun selectMetric(metric: LeaderboardMetric) {
        _uiState.update { it.copy(metric = metric) }
        loadLeaderboard()
    }

    private fun observe(event: WcaEvent) {
        statsJob?.cancel()
        statsJob = viewModelScope.launch {
            statsRepository.observeStats(event).collect { stats -> _uiState.update { it.copy(stats = stats) } }
        }
    }

    private fun loadLeaderboard() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            when (val result = statsRepository.leaderboard(state.event, state.metric, state.scope)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(leaderboard = result.data, isLoading = false) }

                is DataResult.Failure ->
                    _uiState.update { it.copy(isLoading = false, message = result.error.message) }
            }
        }
    }
}
