package com.donik1998.cubeclash.feature.race

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.donik1998.cubeclash.core.domain.common.TimeSource
import com.donik1998.cubeclash.core.domain.realtime.RaceGateway
import com.donik1998.cubeclash.core.model.RaceCommand
import com.donik1998.cubeclash.core.model.RaceEvent
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.RaceStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * The race screen reflects the server; it never advances itself.
 *
 * Every status change comes from an inbound [RaceEvent]. The only clock this class runs is the
 * player's own elapsed time between `solve:start` and `solve:stop`, and even that is a display
 * value — the submitted number is the server's to accept or reject.
 */
@HiltViewModel
class RaceViewModel @Inject constructor(
    private val gateway: RaceGateway,
    private val timeSource: TimeSource,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RaceUiState())
    val uiState: StateFlow<RaceUiState> = _uiState.asStateFlow()

    private var solveStartedAtMs: Long? = null
    private var tickJob: Job? = null
    private var watchdogJob: Job? = null

    init {
        viewModelScope.launch {
            gateway.events().collect(::onEvent)
        }
    }

    fun onAction(action: RaceAction) {
        when (action) {
            is RaceAction.SelectTab -> _uiState.update {
                val tab = action.tab
                it.copy(
                    tab = tab,
                    // Switching to quick match can strand a selection that queue can't serve.
                    event = if (tab == LobbyTab.QUICK && !it.event.quickMatch) {
                        com.donik1998.cubeclash.core.model.WcaEvent.THREE
                    } else {
                        it.event
                    },
                )
            }

            is RaceAction.SelectEvent -> _uiState.update { it.copy(event = action.event) }

            RaceAction.StartMatchmaking -> send(
                RaceCommand.Create(_uiState.value.mode, _uiState.value.event),
            )

            is RaceAction.UpdateJoinCode -> _uiState.update { it.copy(joinCode = action.code.uppercase()) }

            RaceAction.JoinPrivate -> send(RaceCommand.Join(_uiState.value.joinCode))

            RaceAction.Ready -> send(RaceCommand.Ready)

            RaceAction.StopSolve -> stopSolve()

            RaceAction.LeaveRace -> {
                send(RaceCommand.Leave)
                stopTicking()
                _uiState.value = RaceUiState(tab = _uiState.value.tab, event = _uiState.value.event)
            }

            RaceAction.DismissMessage -> _uiState.update { it.copy(message = null) }
        }
    }

    private fun onEvent(event: RaceEvent) {
        restartWatchdog()
        when (event) {
            is RaceEvent.State -> {
                _uiState.update { it.copy(room = event.room, event = event.room.event) }
                if (event.room.status == RaceStatus.RACING) startSolve()
            }

            is RaceEvent.ReadyUpdate -> _uiState.update { state ->
                val room = state.room ?: return@update state
                state.copy(
                    room = room.copy(
                        players = room.players.map { player ->
                            if (player.userId == event.userId) player.copy(isReady = event.ready) else player
                        },
                    ),
                )
            }

            is RaceEvent.Countdown -> _uiState.update { it.copy(countdown = event.value) }

            is RaceEvent.ScrambleRevealed -> _uiState.update {
                it.copy(scramble = event.scramble, countdown = null)
            }

            is RaceEvent.OpponentProgress -> _uiState.update { it.copy(opponentRunningMs = event.runningMs) }

            is RaceEvent.Settled -> {
                stopTicking()
                watchdogJob?.cancel()
                _uiState.update { state ->
                    state.copy(
                        result = event.result,
                        room = state.room?.copy(status = RaceStatus.SETTLED, result = event.result),
                        exitOffered = false,
                    )
                }
            }

            is RaceEvent.Disconnected -> _uiState.update {
                it.copy(message = "Lost the connection. Reconnecting…")
            }

            is RaceEvent.Failed -> _uiState.update { it.copy(message = event.message.ifBlank { null }) }

            is RaceEvent.Unknown -> Unit
        }
    }

    private fun startSolve() {
        if (solveStartedAtMs != null) return
        solveStartedAtMs = timeSource.nowMillis()
        send(RaceCommand.SolveStart)
        tickJob = viewModelScope.launch {
            while (isActive) {
                delay(16)
                val started = solveStartedAtMs ?: break
                _uiState.update { it.copy(yourRunningMs = timeSource.nowMillis() - started) }
            }
        }
    }

    private fun stopSolve() {
        val started = solveStartedAtMs ?: return
        val elapsed = timeSource.nowMillis() - started
        solveStartedAtMs = null
        stopTicking()
        _uiState.update { it.copy(yourTimeMs = elapsed, yourRunningMs = elapsed) }
        // Duplicate submits are ignored server-side once a participant is `submitted`.
        send(RaceCommand.SolveStop(elapsed))
    }

    private fun stopTicking() {
        tickJob?.cancel()
        tickJob = null
    }

    /**
     * Twenty seconds of **silence**, not of waiting: `race:opponent_progress` arrives
     * continuously while the opponent is still solving, so no per-event timeout is needed.
     * It only offers an exit — the server still owns the result, and the submitted time stays
     * with it, so leaving is not a forfeit.
     */
    private fun restartWatchdog() {
        watchdogJob?.cancel()
        watchdogJob = viewModelScope.launch {
            delay(RaceGateway.SILENCE_WATCHDOG_MS)
            if (_uiState.value.isInRace) _uiState.update { it.copy(exitOffered = true) }
        }
    }

    private fun send(command: RaceCommand) {
        viewModelScope.launch { gateway.send(command) }
    }

    override fun onCleared() {
        stopTicking()
        watchdogJob?.cancel()
        super.onCleared()
    }
}
