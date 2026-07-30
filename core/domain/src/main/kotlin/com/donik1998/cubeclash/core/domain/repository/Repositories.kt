package com.donik1998.cubeclash.core.domain.repository

import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.model.AppSettings
import com.donik1998.cubeclash.core.model.AuthState
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardPage
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.model.RaceMode
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.ThemeMode
import com.donik1998.cubeclash.core.model.TimerStyle
import com.donik1998.cubeclash.core.model.User
import com.donik1998.cubeclash.core.model.WcaEvent
import kotlinx.coroutines.flow.Flow

/**
 * The contracts the domain owns. Implementations live in `:core:data` and every one of them
 * ships twice — a real one against `cubeclash-backend`, and a fake that lets the whole app
 * be demoed with no server at all (`cubeclash.useFakeData=true`).
 */
interface SolveRepository {
    /** The current practice session, newest first. Local-first: emits before the network answers. */
    fun observeSession(event: WcaEvent): Flow<List<Solve>>

    fun observeHistory(event: WcaEvent): Flow<List<Solve>>

    suspend fun logSolve(solve: Solve): DataResult<Solve>

    suspend fun updatePenalty(solveId: String, penalty: Penalty): DataResult<Solve>

    suspend fun deleteSolve(solveId: String): DataResult<Unit>

    suspend fun clearSession(event: WcaEvent): DataResult<Unit>

    /** Pushes everything still [com.donik1998.cubeclash.core.model.SyncState.PENDING]. */
    suspend fun sync(): DataResult<Int>
}

interface ScrambleRepository {
    /** Generated on-device by default; `GET /scramble` is not on the critical path. */
    suspend fun next(event: WcaEvent, cubeCount: Int = 3): DataResult<Scramble>
}

interface AuthRepository {
    val authState: Flow<AuthState>

    suspend fun signIn(email: String, password: String): DataResult<User>

    suspend fun signUp(email: String, password: String, displayName: String): DataResult<User>

    suspend fun signOut(): DataResult<Unit>
}

interface UserRepository {
    fun observeMe(): Flow<User?>

    suspend fun refreshMe(): DataResult<User>

    suspend fun updateProfile(displayName: String?, country: String?): DataResult<User>

    /**
     * The public profile of another player via `GET /users/:id`. Distinct from [ProfileRepository],
     * which serves the viewer's own aggregate — this is someone else's page and carries a
     * viewer-relative head-to-head instead of a rank. A payload with no usable identity is dropped
     * by the mapper and surfaces here as a `DataResult.Failure` rather than a half-blank success.
     */
    suspend fun publicProfile(id: String): DataResult<PublicProfile>
}

interface StatsRepository {
    fun observeStats(event: WcaEvent): Flow<EventStats>

    /**
     * One page of the leaderboard for [event]/[metric]/[scope]. [cursor] is `null` for the
     * first page; the returned [LeaderboardPage.nextCursor] drives pagination. The page also
     * carries the viewer's own row separately, since it is almost never on page 1.
     */
    suspend fun leaderboard(
        event: WcaEvent,
        metric: LeaderboardMetric,
        scope: LeaderboardScope,
        cursor: String? = null,
    ): DataResult<LeaderboardPage>
}

interface ProfileRepository {
    /**
     * The You · Profile aggregate for the current viewer, scoped to [event] and [rankScope]
     * (which only affect the rank and best-single fields). A profile that arrives without a
     * usable identity is dropped by the mapper and surfaces here as a `DataResult.Failure`
     * rather than a half-blank success.
     */
    suspend fun profile(
        event: WcaEvent = WcaEvent.THREE,
        rankScope: LeaderboardScope = LeaderboardScope.GLOBAL,
    ): DataResult<PlayerProfile>
}

interface RaceRepository {
    suspend fun createRace(mode: RaceMode, event: WcaEvent): DataResult<RaceRoom>

    suspend fun joinRace(code: String): DataResult<RaceRoom>

    suspend fun history(): DataResult<List<RaceRoom>>
}

/**
 * Where the JWT pair lives.
 *
 * It is a domain contract rather than a data-layer detail because two modules need it and
 * neither should depend on the other: `:core:network` reads it on every request, and
 * `:core:datastore` owns the persistence.
 */
interface TokenStore {
    suspend fun accessToken(): String?

    suspend fun refreshToken(): String?

    suspend fun save(access: String, refresh: String)

    suspend fun clear()

    val isSignedIn: Flow<Boolean>
}

interface SettingsRepository {
    val settings: Flow<AppSettings>

    suspend fun setThemeMode(mode: ThemeMode)

    suspend fun setTimerStyle(style: TimerStyle)

    suspend fun setInspectionEnabled(enabled: Boolean)

    suspend fun setHapticsEnabled(enabled: Boolean)

    suspend fun setLastEvent(event: WcaEvent)
}
