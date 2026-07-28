package com.donik1998.cubeclash.feature.timer

import com.donik1998.cubeclash.core.domain.timer.TimerState
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.TimerStyle
import com.donik1998.cubeclash.core.model.WcaEvent

/** One immutable object per frame; the screen renders it and nothing else. */
data class TimerUiState(
    val event: WcaEvent = WcaEvent.DEFAULT,
    val scramble: Scramble = Scramble.EMPTY,
    val scrambleSource: ScrambleSource = ScrambleSource.RANDOM,
    val timerState: TimerState = TimerState.Idle,
    val stats: EventStats = EventStats(WcaEvent.DEFAULT),
    val session: List<Solve> = emptyList(),
    val timerStyle: TimerStyle = TimerStyle.HOLD,
    val inspectionEnabled: Boolean = true,
    val lastSolve: Solve? = null,
    val isEventPickerOpen: Boolean = false,
    val message: String? = null,
) {
    /** Solving goes full-screen: nav bar hidden, nothing to mis-tap. */
    val isImmersive: Boolean
        get() = timerState is TimerState.Inspecting ||
            timerState is TimerState.Running ||
            timerState is TimerState.Arming

    /** Penalty controls appear only *after* a solve — there is nothing to penalise before one. */
    val showPenaltyControls: Boolean get() = timerState is TimerState.Stopped && lastSolve != null

    val pendingPenalty: Penalty
        get() = when (val state = timerState) {
            is TimerState.Inspecting -> state.pendingPenalty
            is TimerState.Running -> state.carriedPenalty
            is TimerState.Stopped -> state.penalty
            else -> Penalty.NONE
        }
}

sealed interface TimerAction {
    data object PointerDown : TimerAction
    data object PointerUp : TimerAction
    data object NewScramble : TimerAction
    data class SelectEvent(val event: WcaEvent) : TimerAction
    data class SelectScrambleSource(val source: ScrambleSource) : TimerAction
    data class ApplyPenalty(val penalty: Penalty) : TimerAction
    data object DeleteLastSolve : TimerAction
    data object OpenEventPicker : TimerAction
    data object DismissEventPicker : TimerAction
    data object DismissMessage : TimerAction
}
