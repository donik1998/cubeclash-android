package com.donik1998.cubeclash.core.domain.timer

import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.TimerStyle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The clock is a parameter, so the entire inspection ladder is exercised in microseconds
 * instead of being watched by hand for seventeen seconds.
 */
class TimerStateMachineTest {

    private val hold = TimerConfig(style = TimerStyle.HOLD, inspectionEnabled = true)
    private val tapNoInspection = TimerConfig(style = TimerStyle.TAP, inspectionEnabled = false)

    private fun run(config: TimerConfig, vararg events: TimerEvent): TimerState =
        events.fold<TimerEvent, TimerState>(TimerState.Idle) { state, event ->
            TimerStateMachine.reduce(state, event, config)
        }

    @Test
    fun `releasing before the hold threshold is a mis-tap, not a start`() {
        val state = run(hold, TimerEvent.PointerDown(0), TimerEvent.PointerUp(300))
        assertEquals(TimerState.Idle, state)
    }

    @Test
    fun `holding past the threshold arms and then starts inspection`() {
        val state = run(hold, TimerEvent.PointerDown(0), TimerEvent.Tick(600), TimerEvent.PointerUp(600))
        assertTrue(state is TimerState.Inspecting)
    }

    @Test
    fun `arming reports readiness so the pad can turn green`() {
        val state = run(hold, TimerEvent.PointerDown(0), TimerEvent.Tick(560))
        assertTrue((state as TimerState.Arming).isReady)
    }

    @Test
    fun `inspection under fifteen seconds carries no penalty - A3d1`() {
        assertEquals(Penalty.NONE, TimerStateMachine.penaltyFor(14_999))
        assertEquals(Penalty.NONE, TimerStateMachine.penaltyFor(15_000))
    }

    @Test
    fun `starting between fifteen and seventeen seconds is a plus two - A3d1`() {
        assertEquals(Penalty.PLUS_TWO, TimerStateMachine.penaltyFor(15_001))
        assertEquals(Penalty.PLUS_TWO, TimerStateMachine.penaltyFor(17_000))
    }

    @Test
    fun `past seventeen seconds the attempt is a DNF - A3d2`() {
        assertEquals(Penalty.DNF, TimerStateMachine.penaltyFor(17_001))
    }

    @Test
    fun `the inspection penalty is carried into the running solve`() {
        val running = run(
            hold,
            TimerEvent.PointerDown(0),
            TimerEvent.Tick(600),
            TimerEvent.PointerUp(600),
            TimerEvent.PointerUp(16_000),
        )
        assertEquals(Penalty.PLUS_TWO, (running as TimerState.Running).carriedPenalty)
    }

    @Test
    fun `any touch stops a running solve and freezes the elapsed time`() {
        val stopped = run(
            tapNoInspection,
            TimerEvent.PointerUp(1_000),
            TimerEvent.Tick(9_870),
            TimerEvent.PointerDown(11_450),
        )
        assertEquals(TimerState.Stopped(elapsedMs = 10_450, penalty = Penalty.NONE), stopped)
    }

    @Test
    fun `with inspection off a start goes straight to running`() {
        val state = run(tapNoInspection, TimerEvent.PointerUp(0))
        assertTrue(state is TimerState.Running)
    }

    @Test
    fun `a stopped timer only leaves that state on an explicit reset`() {
        val stopped = TimerState.Stopped(9_870, Penalty.NONE)
        assertEquals(stopped, TimerStateMachine.reduce(stopped, TimerEvent.Tick(99_999), hold))
        assertEquals(TimerState.Idle, TimerStateMachine.reduce(stopped, TimerEvent.Reset, hold))
    }
}
