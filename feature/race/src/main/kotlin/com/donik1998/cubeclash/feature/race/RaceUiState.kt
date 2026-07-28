package com.donik1998.cubeclash.feature.race

import com.donik1998.cubeclash.core.model.RaceMode
import com.donik1998.cubeclash.core.model.RaceResult
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.RaceStatus
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.WcaEvent

enum class LobbyTab(val label: String) { QUICK("Quick Match"), PRIVATE("Private"), TOURNAMENTS("Tournaments") }

data class RaceUiState(
    val tab: LobbyTab = LobbyTab.QUICK,
    val event: WcaEvent = WcaEvent.THREE,
    val room: RaceRoom? = null,
    val scramble: Scramble = Scramble.EMPTY,
    val countdown: Int? = null,
    val yourRunningMs: Long = 0,
    val opponentRunningMs: Long = 0,
    val yourTimeMs: Long? = null,
    val result: RaceResult? = null,
    val joinCode: String = "",
    val message: String? = null,
    /**
     * Set by the silence watchdog. The race route blocks back-navigation mid-solve, so the one
     * screen with no way out must not end up with no way out.
     */
    val exitOffered: Boolean = false,
) {
    val status: RaceStatus get() = room?.status ?: RaceStatus.WAITING

    val isInRace: Boolean get() = room != null && status != RaceStatus.SETTLED

    /** Quick match is narrower than raceable: a 6×6 queue is realistically empty. */
    val eventsForTab: List<WcaEvent>
        get() = if (tab == LobbyTab.QUICK) WcaEvent.quickMatch else WcaEvent.raceable

    val mode: RaceMode get() = if (tab == LobbyTab.PRIVATE) RaceMode.PRIVATE else RaceMode.QUICK
}

sealed interface RaceAction {
    data class SelectTab(val tab: LobbyTab) : RaceAction
    data class SelectEvent(val event: WcaEvent) : RaceAction
    data object StartMatchmaking : RaceAction
    data class UpdateJoinCode(val code: String) : RaceAction
    data object JoinPrivate : RaceAction
    data object Ready : RaceAction
    data object StopSolve : RaceAction
    data object LeaveRace : RaceAction
    data object DismissMessage : RaceAction
}
