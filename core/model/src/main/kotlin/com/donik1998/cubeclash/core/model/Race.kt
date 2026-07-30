package com.donik1998.cubeclash.core.model

/**
 * The live race, as the client sees it.
 *
 * The server owns every transition; the client only ever *reflects* [RaceStatus]. That is
 * the whole point of a server-authoritative race — a client that could advance its own
 * state machine could also fake a time.
 */
data class RaceRoom(
    val id: String,
    val mode: RaceMode,
    val event: WcaEvent,
    val status: RaceStatus,
    val code: String? = null,
    val players: List<RacePlayer> = emptyList(),
    val scramble: Scramble = Scramble.EMPTY,
    val countdown: Int? = null,
    val result: RaceResult? = null,
) {
    fun playerOrNull(userId: String): RacePlayer? = players.firstOrNull { it.userId == userId }
    fun opponentOf(userId: String): RacePlayer? = players.firstOrNull { it.userId != userId }
}

enum class RaceMode(val wire: String) { QUICK("quick"), PRIVATE("private") }

/** `waiting → ready-check → countdown → racing → settled`, exactly as the protocol defines it. */
enum class RaceStatus(val wire: String) {
    WAITING("waiting"),
    READY_CHECK("ready-check"),
    COUNTDOWN("countdown"),
    RACING("racing"),
    SETTLED("settled"),
    ;

    companion object {
        fun fromWire(wire: String?): RaceStatus = entries.firstOrNull { it.wire == wire } ?: WAITING
    }
}

data class RacePlayer(
    val userId: String,
    val displayName: String,
    val country: String? = null,
    val elo: Int? = null,
    val isReady: Boolean = false,
    /** Live pace from `race:opponent_progress`, or the player's own running clock. */
    val runningMs: Long? = null,
    val finalTimeMs: Long? = null,
    val penalty: Penalty = Penalty.NONE,
    val hasSubmitted: Boolean = false,
)

data class RaceResult(
    val winnerUserId: String?,
    val yourTimeMs: Long?,
    val opponentTimeMs: Long?,
    val eloDelta: Int?,
) {
    val isDraw: Boolean get() = winnerUserId == null

    val deltaMs: Long?
        get() = if (yourTimeMs != null && opponentTimeMs != null) yourTimeMs - opponentTimeMs else null
}
