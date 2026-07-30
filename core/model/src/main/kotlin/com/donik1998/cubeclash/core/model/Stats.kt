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

/**
 * One ranked row on a leaderboard.
 *
 * Every field is non-nullable except [country]: a row without a rank, a user, a name or a
 * value is meaningless and is dropped by the mapper before it ever reaches here, but a cuber
 * with no country on file is still perfectly rankable — the row simply renders without a
 * country line.
 *
 * [rank] can be shared: the server uses `RANK()`, so two people on the same [valueMs] are
 * both, say, 3rd. [valueMs] is the metric's **ranking value** (a `+2` folded in), not raw
 * `time_ms`; DNFs are unranked and never appear. It is exposed as a raw [Long] so formatting
 * stays the UI's job via [ResultFormatter].
 */
data class LeaderboardEntry(
    val rank: Int,
    val userId: String,
    val displayName: String,
    val country: String?,
    val valueMs: Long,
    val event: WcaEvent,
    /**
     * When the ranking solve was set, or `null` when the server omits or garbles it — a missing
     * timestamp is not worth dropping a ranked row over, and fabricating [java.time.Instant.EPOCH]
     * (1970) would be a claim rather than a blank. iOS and Flutter keep it nullable too.
     */
    val solvedAt: java.time.Instant?,
)

/**
 * A page of leaderboard rows plus the two things the item list cannot carry:
 *  - [nextCursor] for cursor-based pagination (`null` on the last page);
 *  - [viewer], the signed-in user's own row, which is almost never on page 1 (rank ~1,204 in
 *    the design) and so is a separate top-level field rather than a flag inside [entries].
 */
data class LeaderboardPage(
    val entries: List<LeaderboardEntry>,
    val nextCursor: String? = null,
    val viewer: LeaderboardEntry? = null,
)

enum class LeaderboardScope(val wire: String, val label: String) {
    GLOBAL("global", "Global"),
    FRIENDS("friends", "Friends"),
    COUNTRY("country", "Country"),
}

enum class LeaderboardMetric(val wire: String, val label: String) {
    SINGLE("single", "single"),
    AO5("ao5", "ao5"),
    AO12("ao12", "ao12"),
}
