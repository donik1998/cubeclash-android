package com.donik1998.cubeclash.core.network

import com.donik1998.cubeclash.core.network.dto.AuthResponse
import com.donik1998.cubeclash.core.network.dto.CreateRaceRequest
import com.donik1998.cubeclash.core.network.dto.CreateSolveRequest
import com.donik1998.cubeclash.core.network.dto.JoinRaceRequest
import com.donik1998.cubeclash.core.network.dto.LeaderboardResponseDto
import com.donik1998.cubeclash.core.network.dto.LoginRequest
import com.donik1998.cubeclash.core.network.dto.PageDto
import com.donik1998.cubeclash.core.network.dto.ProfileResponseDto
import com.donik1998.cubeclash.core.network.dto.PublicUserResponseDto
import com.donik1998.cubeclash.core.network.dto.RaceDto
import com.donik1998.cubeclash.core.network.dto.RefreshRequest
import com.donik1998.cubeclash.core.network.dto.RegisterRequest
import com.donik1998.cubeclash.core.network.dto.ScrambleListDto
import com.donik1998.cubeclash.core.network.dto.SolveDto
import com.donik1998.cubeclash.core.network.dto.StatsDto
import com.donik1998.cubeclash.core.network.dto.SyncRequest
import com.donik1998.cubeclash.core.network.dto.SyncResponse
import com.donik1998.cubeclash.core.network.dto.UpdatePenaltyRequest
import com.donik1998.cubeclash.core.network.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/** The REST surface, one-for-one with `cubeclash-backend`'s `/v1` prefix. */
interface CubeClashApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @POST("auth/refresh")
    suspend fun refresh(@Body body: RefreshRequest): AuthResponse

    @POST("auth/logout")
    suspend fun logout()

    @GET("me")
    suspend fun me(): UserDto

    @PATCH("me")
    suspend fun updateMe(@Body body: Map<String, String?>): UserDto

    /**
     * The You · Profile aggregate. Additive to the thin `GET /me`, which still returns just
     * `{ user }`. `event`/`rank_scope` scope the rank and best-single fields; the response
     * echoes the event it used so the client never has to guess.
     */
    @GET("me/profile")
    suspend fun profile(
        @Query("event") event: String = "3x3",
        @Query("rank_scope") rankScope: String = "global",
    ): ProfileResponseDto

    /**
     * A public player profile, wrapped in `{ user }`. Viewer-relative: `head_to_head` reflects the
     * caller's own record against this player and is null when the two have never raced.
     */
    @GET("users/{id}")
    suspend fun publicProfile(@Path("id") id: String): PublicUserResponseDto

    @GET("solves")
    suspend fun solves(
        @Query("event") event: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 100,
    ): PageDto<SolveDto>

    @POST("solves")
    suspend fun createSolve(@Body body: CreateSolveRequest): SolveDto

    @PATCH("solves/{id}")
    suspend fun updatePenalty(@Path("id") id: String, @Body body: UpdatePenaltyRequest): SolveDto

    @DELETE("solves/{id}")
    suspend fun deleteSolve(@Path("id") id: String)

    @POST("sync")
    suspend fun sync(@Body body: SyncRequest): SyncResponse

    @GET("scramble")
    suspend fun scramble(
        @Query("event") event: String,
        @Query("count") count: Int = 1,
    ): ScrambleListDto

    @GET("stats")
    suspend fun stats(@Query("event") event: String): StatsDto

    @GET("leaderboard")
    suspend fun leaderboard(
        @Query("event") event: String,
        @Query("metric") metric: String,
        @Query("scope") scope: String,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int? = null,
    ): LeaderboardResponseDto

    @POST("races")
    suspend fun createRace(@Body body: CreateRaceRequest): RaceDto

    @POST("races/join")
    suspend fun joinRace(@Body body: JoinRaceRequest): RaceDto

    @GET("races")
    suspend fun raceHistory(@Query("history") history: Boolean = true): PageDto<RaceDto>
}
