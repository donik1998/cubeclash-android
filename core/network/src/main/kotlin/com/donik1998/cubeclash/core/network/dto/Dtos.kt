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

@Serializable
data class LeaderboardEntryDto(
    val rank: Int,
    val user: UserDto,
    val value: Long,
    @SerialName("is_current_user") val isCurrentUser: Boolean = false,
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
