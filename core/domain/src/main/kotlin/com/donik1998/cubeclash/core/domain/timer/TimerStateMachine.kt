package com.donik1998.cubeclash.core.domain.timer

import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.TimerStyle

/**
 * The solve state machine — pure, clock-injected, and deliberately free of Android.
 *
 * Every transition is a function of (state, event, now). Nothing here starts a coroutine or
 * reads `System.currentTimeMillis()`, which is why the whole 15-second inspection ladder can
 * be tested in microseconds instead of being observed by hand.
 */
object TimerStateMachine {

    /** WCA 4-second-warning / 8-second-warning aside, inspection is 15 seconds (A3b1). */
    const val INSPECTION_MS = 15_000L

    /** A3d1 — starting between 15 and 17 seconds is `+2`. */
    const val INSPECTION_PLUS_TWO_MS = 17_000L

    /**
     * How long the pad must be held before a solve can start. Not a WCA rule — it is the
     * gesture threshold that separates "arming the timer" from "tapping something on top of it".
     */
    const val HOLD_THRESHOLD_MS = 550L

    fun reduce(state: TimerState, event: TimerEvent, config: TimerConfig): TimerState =
        when (state) {
            is TimerState.Idle -> state.onIdle(event, config)
            is TimerState.Arming -> state.onArming(event, config)
            is TimerState.Inspecting -> state.onInspecting(event, config)
            is TimerState.Running -> state.onRunning(event)
            is TimerState.Stopped -> when (event) {
                is TimerEvent.Reset -> TimerState.Idle
                else -> state
            }
        }

    private fun TimerState.Idle.onIdle(event: TimerEvent, config: TimerConfig): TimerState =
        when (event) {
            is TimerEvent.PointerDown -> when (config.style) {
                TimerStyle.HOLD -> TimerState.Arming(pressedAtMs = event.nowMs, heldMs = 0)
                TimerStyle.TAP -> this
            }

            is TimerEvent.PointerUp -> when (config.style) {
                // Tap style commits on release, so a scroll that started on the pad does not
                // accidentally launch a solve.
                TimerStyle.TAP -> start(event.nowMs, config)
                TimerStyle.HOLD -> this
            }

            else -> this
        }

    private fun TimerState.Arming.onArming(event: TimerEvent, config: TimerConfig): TimerState =
        when (event) {
            is TimerEvent.Tick -> copy(heldMs = event.nowMs - pressedAtMs)

            // Releasing before the threshold is a mis-tap, not a start.
            is TimerEvent.PointerUp ->
                if (event.nowMs - pressedAtMs >= HOLD_THRESHOLD_MS) start(event.nowMs, config)
                else TimerState.Idle

            is TimerEvent.Cancel, is TimerEvent.Reset -> TimerState.Idle
            else -> this
        }

    private fun TimerState.Inspecting.onInspecting(event: TimerEvent, config: TimerConfig): TimerState =
        when (event) {
            is TimerEvent.Tick -> copy(elapsedMs = event.nowMs - startedAtMs)

            is TimerEvent.PointerUp -> TimerState.Running(
                startedAtMs = event.nowMs,
                elapsedMs = 0,
                carriedPenalty = penaltyFor(event.nowMs - startedAtMs),
            )

            is TimerEvent.Cancel, is TimerEvent.Reset -> TimerState.Idle
            else -> this
        }

    private fun TimerState.Running.onRunning(event: TimerEvent): TimerState = when (event) {
        is TimerEvent.Tick -> copy(elapsedMs = event.nowMs - startedAtMs)

        // Any touch stops the clock — the whole surface is the stop target.
        is TimerEvent.PointerDown -> TimerState.Stopped(
            elapsedMs = event.nowMs - startedAtMs,
            penalty = carriedPenalty,
        )

        is TimerEvent.Reset -> TimerState.Idle
        else -> this
    }

    private fun start(nowMs: Long, config: TimerConfig): TimerState =
        if (config.inspectionEnabled) {
            TimerState.Inspecting(startedAtMs = nowMs, elapsedMs = 0)
        } else {
            TimerState.Running(startedAtMs = nowMs, elapsedMs = 0, carriedPenalty = Penalty.NONE)
        }

    /** The inspection ladder: clean → `+2` → DNF. */
    fun penaltyFor(inspectionElapsedMs: Long): Penalty = when {
        inspectionElapsedMs <= INSPECTION_MS -> Penalty.NONE
        inspectionElapsedMs <= INSPECTION_PLUS_TWO_MS -> Penalty.PLUS_TWO
        else -> Penalty.DNF
    }
}

data class TimerConfig(
    val style: TimerStyle = TimerStyle.HOLD,
    val inspectionEnabled: Boolean = true,
)

sealed interface TimerState {
    data object Idle : TimerState

    /** Finger down, waiting out [TimerStateMachine.HOLD_THRESHOLD_MS]. */
    data class Arming(val pressedAtMs: Long, val heldMs: Long) : TimerState {
        val isReady: Boolean get() = heldMs >= TimerStateMachine.HOLD_THRESHOLD_MS
    }

    data class Inspecting(val startedAtMs: Long, val elapsedMs: Long) : TimerState {
        val remainingMs: Long get() = TimerStateMachine.INSPECTION_MS - elapsedMs
        val pendingPenalty: Penalty get() = TimerStateMachine.penaltyFor(elapsedMs)
    }

    data class Running(
        val startedAtMs: Long,
        val elapsedMs: Long,
        val carriedPenalty: Penalty,
    ) : TimerState

    data class Stopped(val elapsedMs: Long, val penalty: Penalty) : TimerState
}

sealed interface TimerEvent {
    data class PointerDown(val nowMs: Long) : TimerEvent
    data class PointerUp(val nowMs: Long) : TimerEvent
    data class Tick(val nowMs: Long) : TimerEvent
    data object Cancel : TimerEvent
    data object Reset : TimerEvent
}
