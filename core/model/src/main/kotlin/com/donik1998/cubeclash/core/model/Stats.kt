package com.donik1998.cubeclash.core.model

/**
 * Aggregates for one event. Every field is nullable because "not enough solves yet" is a
 * real, common state that the UI has to render as an em dash rather than a zero.
 *
 * Values are in **ranking units**: milliseconds for timed events, *centi-moves* for Fewest
 * Moves (so a mean of 28.33 moves is `2833`). [SolveResult.DNF_RANK] means the average is a DNF.
 */
data class EventStats(
    val event: WcaEvent,
    val best: Long? = null,
    val ao5: Long? = null,
    val ao12: Long? = null,
    val ao100: Long? = null,
    val mo3: Long? = null,
    val last: Long? = null,
    val solveCount: Int = 0,
) {
    fun value(kind: SessionStatKind): Long? = when (kind) {
        SessionStatKind.BEST -> best
        SessionStatKind.AO5 -> ao5
        SessionStatKind.AO12 -> ao12
        SessionStatKind.MO3 -> mo3
        SessionStatKind.LAST -> last
        SessionStatKind.COUNT -> solveCount.toLong()
    }
}

data class LeaderboardEntry(
    val rank: Int,
    val user: User,
    val value: Long,
    val isCurrentUser: Boolean = false,
)

enum class LeaderboardScope(val wire: String, val label: String) {
    GLOBAL("global", "Global"),
    FRIENDS("friends", "Friends"),
    COUNTRY("country", "Country"),
}

enum class LeaderboardMetric(val wire: String, val label: String) {
    SINGLE("single", "Single"),
    AO5("ao5", "Ao5"),
}
