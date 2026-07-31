package com.donik1998.cubeclash.core.model

/**
 * One entry in the viewer's friends list — the shape the Friends screen renders per row.
 *
 * Mirrors Flutter's `Friend` (the reference client): every numeric value is **raw** (milliseconds,
 * a plain country code), and all human formatting is the UI's job — the model never carries a
 * pre-formatted string.
 *
 * The core identity ([userId], [displayName]) is non-nullable: a friend row missing either is
 * un-renderable and is dropped by the mapper before it reaches here.
 *
 * Nullability is deliberate and each nullable field maps to UI that collapses cleanly:
 *  - [countryCode] → the country segment of the row's subtitle is dropped;
 *  - [avatarUrl] → the avatar falls back to an initials placeholder derived from [displayName];
 *  - [bestSingleMs] → the "best single" stat renders an em dash (this friend has no ranked solve).
 */
data class Friend(
    val userId: String,
    val displayName: String,
    val status: FriendStatus,
    /** ISO 3166-1 alpha-2 (e.g. `UZ`); null when this friend has no country on file. */
    val countryCode: String? = null,
    /** Absolute avatar URL; null when absent, in which case the UI draws initials. */
    val avatarUrl: String? = null,
    /** Best-ever single in milliseconds, DNF excluded; null when this friend has no solves. */
    val bestSingleMs: Long? = null,
    /**
     * True when **they** invited **you** — the only rows the Accept action is offered on. A
     * missing `incoming` on the wire is a plain "not an incoming request", so it defaults to false.
     */
    val incoming: Boolean = false,
)

/**
 * Friendship state, mirroring the server's `friendships.status` enum.
 *
 * [UNKNOWN] is the house rule for enums: an unrecognised wire value maps here rather than being
 * guessed at or silently collapsed into [ACCEPTED] — a value the client has never seen must never
 * quietly become a live friendship.
 */
enum class FriendStatus(val wire: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),

    /** Any wire value the client does not recognise. Never treated as an accepted friendship. */
    UNKNOWN("unknown"),
    ;

    companion object {
        /** Lenient: an unrecognised or missing status maps to [UNKNOWN], never crashes. */
        fun fromWire(wire: String?): FriendStatus =
            entries.firstOrNull { it.wire == wire } ?: UNKNOWN
    }
}
