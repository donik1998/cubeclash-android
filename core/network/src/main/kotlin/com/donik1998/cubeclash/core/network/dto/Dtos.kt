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
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val elo: Int = 1200,
    @SerialName("solve_count") val solveCount: Int = 0,
)

@Serializable
data class AuthResponse(val user: UserDto? = null, val tokens: TokensDto)

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

@Serializable
data class StatsDto(
    val event: String,
    val best: Long? = null,
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
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val elo: Int? = null,
    val ready: Boolean = false,
    @SerialName("time_ms") val timeMs: Long? = null,
    val penalty: String? = null,
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
