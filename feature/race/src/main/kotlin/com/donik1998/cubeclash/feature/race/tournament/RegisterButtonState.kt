package com.donik1998.cubeclash.feature.race.tournament

import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentStatus

/**
 * The four distinct shapes the register affordance can take. Kept as a pure enum with a pure
 * derivation ([from]) so the button-state matrix — the one thing this screen must not get wrong — is
 * covered by a unit test rather than only a preview.
 *
 * The ordering of the checks in [from] is load-bearing:
 *  - [FINISHED] wins first: a finished bracket has no register affordance at all.
 *  - [REGISTERED] wins over [FULL]: a full tournament the viewer is already in must read as
 *    "Registered", never as a locked-out "Full".
 *  - [FULL] only applies when the viewer is *not* registered.
 *  - [REGISTER] is the enabled, joinable state.
 */
enum class RegisterButtonState {
    /** Not registered, room to spare, joinable — the primary, enabled action. */
    REGISTER,

    /** The viewer has already entered. A done state, shown regardless of whether the bracket is full. */
    REGISTERED,

    /** Not registered and the bracket is full — can't join, disabled. */
    FULL,

    /** The tournament is over — no register affordance. */
    FINISHED,

    /** An inert card (e.g. an unrecognised status) — no register affordance. */
    UNAVAILABLE,
    ;

    companion object {
        fun from(tournament: Tournament): RegisterButtonState = when {
            tournament.status == TournamentStatus.FINISHED -> FINISHED
            // Registered beats full: already-in must not read as locked-out.
            tournament.registered -> REGISTERED
            tournament.isFull -> FULL
            tournament.status == TournamentStatus.UPCOMING ||
                tournament.status == TournamentStatus.LIVE -> REGISTER
            // UNKNOWN or any status that doesn't allow joining.
            else -> UNAVAILABLE
        }
    }
}
