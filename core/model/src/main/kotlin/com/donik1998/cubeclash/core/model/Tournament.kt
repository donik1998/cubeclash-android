package com.donik1998.cubeclash.core.model

import java.time.Instant

/**
 * A tournament as the lobby lists it — mirrors Flutter's `Tournament` (the reference client).
 *
 * **Server-owned in production.** Entrant counts, live status and results all come from the
 * backend. Everything numeric here is **raw** (a plain count, an [Instant]); all human formatting
 * is the UI's job.
 *
 * The core identity ([id], [name]) is non-nullable: a card missing either is un-renderable and is
 * dropped by the mapper before it reaches here. The rest carry neutral defaults chosen by the
 * mapper ([entrants]/[capacity] default 0, [description] blank, [registered] false, [startsAt]
 * null when unparseable) — see `TournamentMappers`.
 *
 * [isFull] is **derived, never a wire field**: it is a conclusion about [entrants] and [capacity],
 * and the house rule keeps conclusions out of stored/wire state so a stale server flag can never
 * contradict the counts it was computed from.
 */
data class Tournament(
    val id: String,
    val name: String,
    /** The `WcaEvent.id` it is raced on — lenient, so an unknown id degrades to 3×3 not a crash. */
    val event: WcaEvent,
    val status: TournamentStatus,
    val entrants: Int,
    val capacity: Int,
    /**
     * When the bracket starts. Null when the wire timestamp is missing or unparseable — a missing
     * start time is not worth dropping the whole card over, and fabricating epoch (1970) would be
     * a claim rather than a blank, so the UI renders "time TBD" instead.
     */
    val startsAt: Instant?,
    val description: String,
    /**
     * Whether the signed-in viewer has entered. Server truth; the fake toggles it locally so the
     * register flow is demoable. A missing value on the wire is a plain "not registered".
     */
    val registered: Boolean = false,
) {
    /** Derived, never stored: a bracket is full once entrants reach capacity. */
    val isFull: Boolean get() = entrants >= capacity
}

/**
 * Where a tournament is in its life, mirroring the server's tournament status enum.
 *
 * [UNKNOWN] is the house rule for enums: an unrecognised wire value maps here rather than being
 * guessed at. The UI treats it like an inert card (no live badge, no register affordance) rather
 * than crashing.
 */
enum class TournamentStatus(val wire: String) {
    UPCOMING("upcoming"),
    LIVE("live"),
    FINISHED("finished"),

    /** Any wire value the client does not recognise. */
    UNKNOWN("unknown"),
    ;

    companion object {
        /** Lenient: an unrecognised or missing status maps to [UNKNOWN], never crashes. */
        fun fromWire(wire: String?): TournamentStatus =
            entries.firstOrNull { it.wire == wire } ?: UNKNOWN
    }
}

/**
 * A full tournament view — a [Tournament] header plus its single-elimination bracket, in round
 * order (earliest first, e.g. Round of 16 → … → Final). Mirrors Flutter's `TournamentDetail`.
 */
data class TournamentDetail(
    val tournament: Tournament,
    /** Earliest round first. Empty when the bracket has not been drawn yet. */
    val rounds: List<TournamentRound>,
)

/** One round of a single-elimination bracket. */
data class TournamentRound(
    val name: String,
    val matches: List<TournamentMatch>,
)

/**
 * One head-to-head in the bracket. Mirrors Flutter's `TournamentMatch`.
 *
 * Times are null before the match is solved; [winner] is the winning seat (`"A"` / `"B"`) or null
 * while the match is pending — [isPlayed] is derived from it.
 */
data class TournamentMatch(
    val playerA: String,
    val playerB: String,
    /** Player A's time in milliseconds; null until the match is played. */
    val timeAMs: Long? = null,
    /** Player B's time in milliseconds; null until the match is played. */
    val timeBMs: Long? = null,
    /** `"A"`, `"B"`, or null while the match has not been played. */
    val winner: String? = null,
) {
    /** Derived, never stored: a match is played once it has a winner. */
    val isPlayed: Boolean get() = winner != null
}
