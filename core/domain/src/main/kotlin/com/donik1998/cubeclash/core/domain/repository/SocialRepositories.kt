package com.donik1998.cubeclash.core.domain.repository

import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentDetail

/**
 * The viewer's friends and the invite/accept flow.
 *
 * ⚠️ **Every method here targets an endpoint that does not exist on `cubeclash-backend` yet.** The
 * `friends` module is a bare `.module.ts` with no controller or routes, so `GET /friends`,
 * `POST /friends/invite` and `POST /friends/{id}/accept` all 404 today. The contract is taken from
 * the vault's API Design doc ("Social & tournaments (roadmap)") and mirrors the Flutter reference
 * client — it is a documented intent, NOT an observed response, and must be re-verified against a
 * real server before anyone trusts the real implementation. Until then the fake is the working
 * path (`cubeclash.useFakeData=true`, the default build).
 */
interface FriendsRepository {
    /** `GET /friends`. One malformed row is dropped by the mapper, never the whole list. */
    suspend fun friends(): DataResult<List<Friend>>

    /** `POST /friends/invite`. [query] resolves to a user server-side (handle or email). */
    suspend fun invite(query: String): DataResult<Unit>

    /** `POST /friends/{id}/accept`. Only valid for an incoming request. */
    suspend fun accept(userId: String): DataResult<Unit>
}

/**
 * The tournament lobby, a bracket view, and registration.
 *
 * Kept **separate from [RaceRepository]** on purpose: that repository owns the live, server-
 * authoritative race lifecycle (create / join / history), whereas tournaments are a distinct
 * concern — a lobby listing, a single-elimination bracket and a register action, on their own
 * screen with their own fake. Folding them into [RaceRepository] would widen it past its single
 * responsibility and force the race fake to carry tournament state it never uses.
 *
 * ⚠️ **Every method here targets an endpoint that does not exist on `cubeclash-backend` yet.** The
 * `tournaments` module is a bare `.module.ts`; `GET /tournaments` and `POST /tournaments/{id}/register`
 * are the roadmap's two endpoints, and `GET /tournaments/{id}` is inferred from the Flutter client
 * (a [TournamentDetail] can't be built without it) — so it is doubly unverified. All shapes come
 * from the API Design doc / the client, not an observed response, and must be re-verified against a
 * real server. The fake is the working path until then.
 */
interface TournamentRepository {
    /** `GET /tournaments`. One malformed card is dropped by the mapper, never the whole list. */
    suspend fun tournaments(): DataResult<List<Tournament>>

    /**
     * `GET /tournaments/{id}` — header plus bracket. ⚠️ Not one of the roadmap's five endpoints;
     * inferred from the Flutter client.
     */
    suspend fun tournament(id: String): DataResult<TournamentDetail>

    /** `POST /tournaments/{id}/register`. */
    suspend fun register(id: String): DataResult<Unit>
}
