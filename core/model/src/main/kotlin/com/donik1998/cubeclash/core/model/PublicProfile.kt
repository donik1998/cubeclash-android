package com.donik1998.cubeclash.core.model

/**
 * A **public** player profile — what `GET /users/:id` returns for *someone else*, viewed from the
 * current account. Distinct from [PlayerProfile], which is the viewer's own aggregate from
 * `GET /me/profile`: different endpoint, different source, different semantics (no rank, no friend
 * count, no win-rate — but a viewer-relative head-to-head the self-profile can't have). They are
 * deliberately separate types so neither is quietly widened to serve the other.
 *
 * Everything numeric here is **raw**: milliseconds and a plain Elo integer. All human formatting
 * (2-dp seconds, localized country name, the em dash for absent values) is the UI's job — the
 * model never carries a pre-formatted string.
 *
 * The [bestSingleMs] / [bestAo5Ms] / [bestAo12Ms] fields are **best-ever** values, NOT the current
 * rolling average. The identically-named `ao5`/`ao12` on `GET /stats` mean the *current* rolling
 * average; these are named `best…` so the two can never be confused at the call site.
 *
 * Nullability is deliberate and each nullable field maps to UI that collapses cleanly:
 *  - [country] → the country segment of the subtitle is dropped;
 *  - [bestSingleMs] / [bestAo5Ms] / [bestAo12Ms] → the corresponding stat tile renders an em dash
 *    (a player with no solves has all three null — verified on a fresh account);
 *  - [headToHead] → **null means "you two have never raced", which is NOT 0-0.** The UI must
 *    distinguish the two: null hides the head-to-head row entirely, whereas a present `0-0` shows
 *    "0 – 0" (you have raced, neither has won). Collapsing null to zero would fabricate a history.
 *
 * The core identity ([id], [displayName]) is non-nullable: a profile missing either is
 * un-renderable and is dropped by the mapper before it reaches here.
 */
data class PublicProfile(
    val id: String,
    val displayName: String,
    /** ISO 3166-1 alpha-2 (e.g. `UZ`); null when this cuber has no country on file. */
    val country: String?,
    /** Stored `users.elo`; never recomputed. Defaults to 1000 when absent (server default). */
    val elo: Int,
    /** Best-ever single in milliseconds, DNF excluded; null when this cuber has no solves yet. */
    val bestSingleMs: Long?,
    /** Best-ever average-of-5 in milliseconds; null when there aren't enough solves for an ao5. */
    val bestAo5Ms: Long?,
    /** Best-ever average-of-12 in milliseconds; null when there aren't enough solves for an ao12. */
    val bestAo12Ms: Long?,
    /**
     * The viewer's record against this player. **Null when they have never raced** — a genuinely
     * different state from a present `HeadToHead(0, 0)`, which means they have raced but nobody has
     * won. The UI keeps them distinct; the mapper never invents a zero-zero for absence.
     */
    val headToHead: HeadToHead?,
)

/**
 * The viewer-relative race record against another player, as returned inside `GET /users/:id`'s
 * `head_to_head` object. Present only when the two accounts have at least one settled race; the
 * absence of the object (null [PublicProfile.headToHead]) means "never raced" and is not this.
 *
 * When the object *is* present but a count is missing on the wire, the mapper defaults it to 0 —
 * a neutral "none on that side" that reads identically to absent, since the object's very presence
 * already establishes that they have raced.
 */
data class HeadToHead(
    val wins: Int,
    val losses: Int,
)
