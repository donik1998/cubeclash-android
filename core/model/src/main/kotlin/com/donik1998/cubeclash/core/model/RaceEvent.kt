package com.donik1998.cubeclash.core.model

/**
 * The inbound half of the race protocol, typed.
 *
 * Names mirror the wire events one-for-one (`race:state`, `race:countdown`, …) so the socket
 * log and the Kotlin `when` read the same. Anything the server sends that this client does not
 * know becomes [Unknown] rather than an exception — a protocol addition must not kill a race
 * in flight.
 */
sealed interface RaceEvent {
    data class State(val room: RaceRoom) : RaceEvent
    data class ReadyUpdate(val userId: String, val ready: Boolean) : RaceEvent
    data class Countdown(val value: Int) : RaceEvent
    data class ScrambleRevealed(val scramble: Scramble) : RaceEvent
    data class OpponentProgress(val userId: String, val runningMs: Long) : RaceEvent
    data class Settled(val result: RaceResult) : RaceEvent
    data class Disconnected(val reason: String?) : RaceEvent
    data class Failed(val message: String) : RaceEvent
    data class Unknown(val name: String) : RaceEvent
}

/** The outbound half. */
sealed interface RaceCommand {
    data class Create(val mode: RaceMode, val event: WcaEvent) : RaceCommand
    data class Join(val code: String) : RaceCommand
    data object Ready : RaceCommand
    data object SolveStart : RaceCommand
    data class SolveStop(val clientTimeMs: Long) : RaceCommand
    data object Leave : RaceCommand
}
