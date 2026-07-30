package com.donik1998.cubeclash.core.model

/**
 * The You · Profile read-model — the six values the screen renders (user identity, rank, summary
 * stats, friend count), aggregated by `GET /me/profile` into one round trip.
 *
 * Everything here is **raw**: milliseconds, a 0..1 ratio, an alpha-2 country code, plain numbers.
 * All human formatting (2-dp seconds, "%", localized country name, thousands separators, "#") is
 * the UI's job — the model never carries a pre-formatted string.
 *
 * Only three fields are nullable, and each maps to a piece of UI that collapses cleanly:
 *  - [country] → the country segment of the subtitle is dropped;
 *  - [rank] → the "#…" segment of the subtitle is dropped;
 *  - [bestSingleMs] / [winRate] → the corresponding stat tile renders an em dash.
 *
 * The core identity ([id], [displayName]) is non-nullable: a profile missing either is
 * un-renderable and is dropped by the mapper before it reaches here.
 */
data class PlayerProfile(
    val id: String,
    val displayName: String,
    /** ISO 3166-1 alpha-2 (e.g. `UZ`); null when the cuber has no country on file. */
    val country: String?,
    /** Stored `users.elo`; never recomputed. Defaults to 1000 when absent (profile default). */
    val elo: Int,
    /** Null when the viewer has no ranked solve in the requested event/scope. */
    val rank: ProfileRank?,
    /** Best single in milliseconds, DNF excluded; null when the viewer has no ranked solve. */
    val bestSingleMs: Long?,
    /** The event [bestSingleMs] is for; echoes the request so the client never guesses. */
    val bestSingleEvent: WcaEvent,
    val totalSolves: Int,
    /** Win ratio 0..1; null when the viewer has no settled races (wins + losses == 0). */
    val winRate: Double?,
    val wins: Int,
    val losses: Int,
    val friendCount: Int,
)

/**
 * The viewer's own ranked position for one event/metric/scope. Rank in this codebase is
 * per-event and per-scope — there is no single "global rank" column — so the tuple travels
 * alongside [position] rather than being assumed.
 */
data class ProfileRank(
    val position: Int,
    val event: WcaEvent,
    val metric: String,
    val scope: LeaderboardScope,
)
