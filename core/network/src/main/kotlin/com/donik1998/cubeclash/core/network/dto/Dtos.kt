package com.donik1998.cubeclash.core.network.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Wire types. `snake_case` on the wire, camelCase in Kotlin, and nothing above `:core:data`
 * ever sees one of these — the mappers are the border.
 */
@Serializable
data class ErrorEnvelope(val error: ApiError)

@Serializable
data class ApiError(
    val code: String,
    val message: String,
    val details: Map<String, String> = emptyMap(),
)

@Serializable
data class TokensDto(
    @SerialName("access") val access: String,
    @SerialName("refresh") val refresh: String,
)

@Serializable
data class UserDto(
    val id: String,
    @SerialName("display_name") val displayName: String,
    val email: String? = null,
    val country: String? = null,
    val elo: Int = 1000,
    @SerialName("solve_count") val solveCount: Int = 0,
)

@Serializable
data class AuthResponse(val user: UserDto? = null, val tokens: TokensDto)

/**
 * The `GET /me` and `PATCH /me` envelope. The server wraps the user in `{"user": …}` exactly like
 * register/login and `GET /users/:id` — it does **not** return a bare user object. The wrapper and
 * the user are nullable so decoding `{}` never throws; the mapper is where a missing user becomes a
 * dropped/failed result.
 */
@Serializable
data class MeResponse(val user: UserDto? = null)

/**
 * The `POST /solves` and `PATCH /solves/:id` envelope. The server wraps the created/updated solve
 * in `{"solve": …}` — it does **not** return a bare solve object. Nullable so `{}` never throws.
 */
@Serializable
data class SolveResponse(val solve: SolveDto? = null)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    @SerialName("display_name") val displayName: String,
)

@Serializable
data class LoginRequest(val email: String, val password: String)

@Serializable
data class RefreshRequest(val refresh: String)

@Serializable
data class SolveDto(
    val id: String,
    @SerialName("client_id") val clientId: String,
    val event: String,
    /** Plain string, newline-separated. `\n` is significant and must round-trip. */
    val scramble: String,
    @SerialName("scramble_source") val scrambleSource: String,
    @SerialName("time_ms") val timeMs: Long,
    val penalty: String,
    @SerialName("solved_at") val solvedAt: String,
    @SerialName("is_pb") val isPb: Boolean = false,
    // The three long-form columns. Absent means *inapplicable*, which an explicit null
    // would not say — so they are omitted entirely rather than sent as null.
    @SerialName("move_count") val moveCount: Int? = null,
    @SerialName("solved_count") val solvedCount: Int? = null,
    @SerialName("attempted_count") val attemptedCount: Int? = null,
)

@Serializable
data class CreateSolveRequest(
    val event: String,
    val scramble: String,
    @SerialName("scramble_source") val scrambleSource: String,
    @SerialName("time_ms") val timeMs: Long,
    val penalty: String,
    @SerialName("solved_at") val solvedAt: String,
    @SerialName("client_id") val clientId: String,
    @SerialName("move_count") val moveCount: Int? = null,
    @SerialName("solved_count") val solvedCount: Int? = null,
    @SerialName("attempted_count") val attemptedCount: Int? = null,
)

@Serializable
data class UpdatePenaltyRequest(val penalty: String)

@Serializable
data class PageDto<T>(
    val items: List<T> = emptyList(),
    @SerialName("next_cursor") val nextCursor: String? = null,
)

@Serializable
data class ScrambleListDto(val scrambles: List<String> = emptyList())

/**
 * `GET /stats` — the server's aggregate over all of a user's solves for one event. **Every field is
 * nullable** so decoding `{}` never throws.
 *
 * The best-single value is on the wire as `best_single_ms`, NOT `best`; reading the wrong key left
 * the field permanently null and blanked the best-single tile. `mo3` is not part of this shape.
 */
@Serializable
data class StatsDto(
    val event: String? = null,
    @SerialName("best_single_ms") val best: Long? = null,
    val ao5: Long? = null,
    val ao12: Long? = null,
    val ao100: Long? = null,
    val mo3: Long? = null,
    @SerialName("solve_count") val solveCount: Int = 0,
)

/**
 * One row of `GET /leaderboard`. **Every field is nullable** — the server is not trustworthy
 * about shape, and decoding `{}` must not throw. The mapper (`:core:data`) is the single place
 * that decides what a missing field means:
 *  - missing `rank`, `user_id`, `value_ms` or `display_name` → the row is dropped;
 *  - missing `country` → the row survives with a null country.
 */
@Serializable
data class LeaderboardItemDto(
    val rank: Int? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    /** ISO 3166-1 alpha-2, nullable on the wire and nullable in the domain. */
    val country: String? = null,
    /** The metric's ranking value in milliseconds. A `+2` is folded in; a DNF never appears. */
    @SerialName("value_ms") val valueMs: Long? = null,
    val event: String? = null,
    @SerialName("solved_at") val solvedAt: String? = null,
)

/**
 * The `GET /leaderboard` envelope. `items` and every element are nullable; `viewer` is a
 * separate top-level field (the viewer's rank is almost never on page 1, so returning it inside
 * `items` would be a lie about pagination).
 */
@Serializable
data class LeaderboardResponseDto(
    val items: List<LeaderboardItemDto?>? = null,
    @SerialName("next_cursor") val nextCursor: String? = null,
    val viewer: LeaderboardItemDto? = null,
)

/**
 * The `GET /me/profile` aggregate — the six values the You · Profile screen needs in one round
 * trip (user + rank + stats + friend count). **Every field is nullable** and every nested object
 * is nullable, so decoding `{}` never throws; the mapper (`:core:data`) is the single place that
 * decides what a missing field means.
 *
 * There is deliberately **no `avatar_url`** here or on `ProfileUserDto`: the avatar is an
 * initials placeholder derived from `display_name`, with no wire field and no column (spec §11.1).
 */
@Serializable
data class ProfileResponseDto(
    val user: ProfileUserDto? = null,
    /** Null when the viewer has no ranked solve in the requested `event`/`rank_scope`. */
    val rank: ProfileRankDto? = null,
    val stats: ProfileStatsDto? = null,
    @SerialName("friend_count") val friendCount: Int? = null,
)

@Serializable
data class ProfileUserDto(
    val id: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    /** ISO 3166-1 alpha-2, nullable on the wire and nullable in the domain. */
    val country: String? = null,
    val elo: Int? = null,
)

@Serializable
data class ProfileRankDto(
    val event: String? = null,
    val metric: String? = null,
    val scope: String? = null,
    val position: Int? = null,
)

@Serializable
data class ProfileStatsDto(
    /** Best single in milliseconds, DNF excluded. NOT the string "8.42". */
    @SerialName("best_single_ms") val bestSingleMs: Long? = null,
    @SerialName("best_single_event") val bestSingleEvent: String? = null,
    @SerialName("total_solves") val totalSolves: Int? = null,
    /** Win ratio 0..1, null when wins + losses == 0. NOT the string "68%". */
    @SerialName("win_rate") val winRate: Double? = null,
    val wins: Int? = null,
    val losses: Int? = null,
)

/**
 * The `GET /users/:id` envelope — a public player profile, wrapped in `{"user": …}` exactly like
 * [ProfileResponseDto]. The wrapper itself and every nested field is nullable, so decoding `{}`
 * never throws; the mapper (`:core:data`) is the single place that decides what a missing field
 * means.
 */
@Serializable
data class PublicUserResponseDto(val user: PublicUserDto? = null)

/**
 * One public player as returned by `GET /users/:id`. **Every field is nullable except [id]** —
 * the house rule: identity is load-bearing, so an un-renderable row (no id) is dropped by the
 * mapper rather than fabricated.
 *
 * `ao5`/`ao12` here are **best-ever** values (mapped to `bestAo5Ms`/`bestAo12Ms`), NOT the current
 * rolling averages that the identically-named fields on [StatsDto] carry. `best_single_ms`, `ao5`
 * and `ao12` are all null for a fresh account with no solves (verified against the live server).
 *
 * There is deliberately **no `avatar_url` and no `solve_count`** on this endpoint — the avatar is
 * an initials placeholder derived from `display_name`, and solve count is not part of the public
 * shape. Neither is invented here.
 */
@Serializable
data class PublicUserDto(
    val id: String,
    @SerialName("display_name") val displayName: String? = null,
    /** ISO 3166-1 alpha-2, nullable on the wire and nullable in the domain. */
    val country: String? = null,
    val elo: Int? = null,
    /** Best-ever single in milliseconds. Null for an account with no solves. */
    @SerialName("best_single_ms") val bestSingleMs: Long? = null,
    /** Best-ever average-of-5 in milliseconds. Null with too few solves. NOT the rolling ao5. */
    val ao5: Long? = null,
    /** Best-ever average-of-12 in milliseconds. Null with too few solves. NOT the rolling ao12. */
    val ao12: Long? = null,
    /**
     * The viewer's record against this player. **Null means "never raced"** and is a distinct
     * state from a present `{wins, losses}` (which may itself be `{0, 0}`). Kept nullable all the
     * way to the model.
     */
    @SerialName("head_to_head") val headToHead: HeadToHeadDto? = null,
)

/** The `head_to_head` object. Both counts nullable; the mapper defaults a missing one to 0. */
@Serializable
data class HeadToHeadDto(
    val wins: Int? = null,
    val losses: Int? = null,
)

@Serializable
data class CreateRaceRequest(val mode: String, val event: String)

@Serializable
data class JoinRaceRequest(val code: String)

@Serializable
data class RaceDto(
    @SerialName("race_id") val raceId: String,
    val mode: String? = null,
    val event: String? = null,
    val status: String? = null,
    val code: String? = null,
    val players: List<RacePlayerDto> = emptyList(),
    val scramble: String? = null,
)

@Serializable
data class RacePlayerDto(
    @SerialName("user_id") val userId: String,
    @SerialName("display_name") val displayName: String,
    val country: String? = null,
    val elo: Int? = null,
    val ready: Boolean = false,
    @SerialName("time_ms") val timeMs: Long? = null,
    val penalty: String? = null,
)

// ---------------------------------------------------------------------------------------------
// Social & tournaments (roadmap).
//
// ⚠️ The `friends` and `tournaments` modules do NOT exist on `cubeclash-backend` yet — each is a
// bare `.module.ts` with no controller, service or routes, so every endpoint below returns 404
// today. These shapes come from the vault's `03 Engineering/API Design.md` ("Social & tournaments
// (roadmap)") and mirror the Flutter reference client, NOT an observed response. They MUST be
// re-verified against a real server response before anyone trusts them. In the meantime the fake
// repositories are the working path (`cubeclash.useFakeData=true`, the default build).
//
// Every field is nullable except identity, per the house rule: decoding `{}` must never throw, and
// the mappers in `:core:data` are the single place that decides what a missing field means.
// ---------------------------------------------------------------------------------------------

/**
 * One row of `GET /friends`. **Every field is nullable except the wire has no single required id**
 * — `user_id` and `display_name` are load-bearing but the DTO still lets them be null so `{}`
 * decodes; the mapper drops a row missing either. Shape is from the API Design doc (roadmap),
 * unverified against a live server.
 */
@Serializable
data class FriendDto(
    @SerialName("user_id") val userId: String? = null,
    @SerialName("display_name") val displayName: String? = null,
    /** `friendships.status`: `pending` | `accepted`. Unknown values map to the unknown case. */
    val status: String? = null,
    /** ISO 3166-1 alpha-2, nullable on the wire and nullable in the domain. */
    val country: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    @SerialName("best_single_ms") val bestSingleMs: Long? = null,
    /** True when *they* invited *you* — gates the Accept action. Missing means false. */
    val incoming: Boolean? = null,
)

/**
 * The `GET /friends` envelope — cursor pagination, matching every other list on this API
 * (`{ items, next_cursor }`). Both the list and every element are nullable so `{}` never throws.
 * Shape from the API Design doc (roadmap), unverified.
 */
@Serializable
data class FriendListResponseDto(
    val items: List<FriendDto?>? = null,
    @SerialName("next_cursor") val nextCursor: String? = null,
)

/** Body for `POST /friends/invite`. The server resolves the query to a user. */
@Serializable
data class FriendInviteRequest(val query: String)

/**
 * One row of `GET /tournaments`. **Every field is nullable**; `id` and `name` are load-bearing and
 * the mapper drops a card missing either. `entrants`/`capacity` are counts and `is_full` is
 * deliberately NOT a wire field — the client derives fullness from the two counts so a stale server
 * flag can never contradict them. Shape from the API Design doc (roadmap), unverified.
 */
@Serializable
data class TournamentDto(
    val id: String? = null,
    val name: String? = null,
    /** `WcaEvent.id`. Unknown ids degrade to 3×3 in the mapper. */
    val event: String? = null,
    /** `upcoming` | `live` | `finished`. Unknown values map to the unknown case. */
    val status: String? = null,
    val entrants: Int? = null,
    val capacity: Int? = null,
    /** ISO 8601 with an explicit UTC offset. Parsed leniently; unparseable stays null. */
    @SerialName("starts_at") val startsAt: String? = null,
    val description: String? = null,
    /** Whether the viewer has entered. Missing means false. */
    val registered: Boolean? = null,
)

/**
 * The `GET /tournaments` envelope — cursor pagination like every other list. Shape from the API
 * Design doc (roadmap), unverified.
 */
@Serializable
data class TournamentListResponseDto(
    val items: List<TournamentDto?>? = null,
    @SerialName("next_cursor") val nextCursor: String? = null,
)

/**
 * The `GET /tournaments/{id}` detail envelope: the tournament header plus its bracket. This detail
 * route is NOT in the roadmap's five endpoints — it is inferred from Flutter's client (which calls
 * it) because a [TournamentDetail] cannot be assembled without it. Doubly unverified. Every field
 * nullable; the mapper drops the whole detail if the tournament header has no usable identity.
 */
@Serializable
data class TournamentDetailResponseDto(
    val tournament: TournamentDto? = null,
    val rounds: List<TournamentRoundDto?>? = null,
)

/** One round of a single-elimination bracket. Shape from the client, unverified. */
@Serializable
data class TournamentRoundDto(
    val name: String? = null,
    val matches: List<TournamentMatchDto?>? = null,
)

/** One head-to-head in the bracket. Times null before a match is played. Unverified. */
@Serializable
data class TournamentMatchDto(
    @SerialName("player_a") val playerA: String? = null,
    @SerialName("player_b") val playerB: String? = null,
    @SerialName("time_a_ms") val timeAMs: Long? = null,
    @SerialName("time_b_ms") val timeBMs: Long? = null,
    /** `"A"`, `"B"`, or null while pending. */
    val winner: String? = null,
)

@Serializable
data class SyncRequest(
    val since: String? = null,
    val solves: List<CreateSolveRequest> = emptyList(),
)

@Serializable
data class SyncResponse(
    val applied: Int = 0,
    val changes: List<SolveDto> = emptyList(),
    @SerialName("server_time") val serverTime: String? = null,
)
