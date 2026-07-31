package com.donik1998.cubeclash.core.data.fake

import com.donik1998.cubeclash.core.data.demo.DemoSeed
import com.donik1998.cubeclash.core.domain.common.AppError
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.AuthRepository
import com.donik1998.cubeclash.core.domain.repository.FriendsRepository
import com.donik1998.cubeclash.core.domain.repository.ProfileRepository
import com.donik1998.cubeclash.core.domain.repository.RaceRepository
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.domain.repository.StatsRepository
import com.donik1998.cubeclash.core.domain.repository.TournamentRepository
import com.donik1998.cubeclash.core.domain.repository.UserRepository
import com.donik1998.cubeclash.core.domain.stats.SessionStatsCalculator
import com.donik1998.cubeclash.core.model.AuthState
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.FriendStatus
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardPage
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.model.RaceMode
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.RaceStatus
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.SyncState
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentDetail
import com.donik1998.cubeclash.core.model.User
import com.donik1998.cubeclash.core.model.WcaEvent
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * The in-memory half of the app.
 *
 * These are not test doubles bolted on afterwards — they are how CubeClash stays demoable
 * while `cubeclash-backend` is being built, and they are what every screenshot and UI test
 * runs against. Same interfaces, same shapes, no server.
 */
@Singleton
class FakeSolveRepository @Inject constructor(
    private val demoSeed: DemoSeed,
) : SolveRepository {

    private val sessions = mutableMapOf<WcaEvent, MutableStateFlow<List<Solve>>>()

    private fun stateOf(event: WcaEvent): MutableStateFlow<List<Solve>> =
        sessions.getOrPut(event) { MutableStateFlow(demoSeed.solves(event)) }

    override fun observeSession(event: WcaEvent): Flow<List<Solve>> = stateOf(event)

    override fun observeHistory(event: WcaEvent): Flow<List<Solve>> = stateOf(event)

    override suspend fun logSolve(solve: Solve): DataResult<Solve> {
        val stored = solve.copy(syncState = SyncState.SYNCED)
        stateOf(solve.event).value = listOf(stored) + stateOf(solve.event).value
        return DataResult.Success(stored)
    }

    override suspend fun updatePenalty(solveId: String, penalty: Penalty): DataResult<Solve> {
        var updated: Solve? = null
        sessions.values.forEach { state ->
            state.value = state.value.map { solve ->
                if (solve.id == solveId) solve.copy(penalty = penalty).also { updated = it } else solve
            }
        }
        return updated?.let { DataResult.Success(it) }
            ?: DataResult.Failure(AppError.NotFound("That solve is gone."))
    }

    override suspend fun deleteSolve(solveId: String): DataResult<Unit> {
        sessions.values.forEach { state ->
            state.value = state.value.filterNot { it.id == solveId }
        }
        return DataResult.Success(Unit)
    }

    override suspend fun clearSession(event: WcaEvent): DataResult<Unit> {
        stateOf(event).value = emptyList()
        return DataResult.Success(Unit)
    }

    override suspend fun sync(): DataResult<Int> = DataResult.Success(0)
}

@Singleton
class FakeAuthRepository @Inject constructor(
    private val demoSeed: DemoSeed,
) : AuthRepository, UserRepository {

    private val state = MutableStateFlow<AuthState>(AuthState.SignedIn(demoSeed.demoUser))

    override val authState: Flow<AuthState> = state

    override fun observeMe(): Flow<User?> = state.map { (it as? AuthState.SignedIn)?.user }

    override suspend fun signIn(email: String, password: String): DataResult<User> {
        val user = demoSeed.demoUser.copy(email = email)
        state.value = AuthState.SignedIn(user)
        return DataResult.Success(user)
    }

    override suspend fun signUp(email: String, password: String, displayName: String): DataResult<User> {
        val user = demoSeed.demoUser.copy(email = email, displayName = displayName)
        state.value = AuthState.SignedIn(user)
        return DataResult.Success(user)
    }

    override suspend fun signOut(): DataResult<Unit> {
        state.value = AuthState.SignedOut
        return DataResult.Success(Unit)
    }

    override suspend fun refreshMe(): DataResult<User> =
        (state.value as? AuthState.SignedIn)?.let { DataResult.Success(it.user) }
            ?: DataResult.Failure(AppError.Unauthorized)

    override suspend fun updateProfile(displayName: String?, country: String?): DataResult<User> {
        val current = (state.value as? AuthState.SignedIn)?.user
            ?: return DataResult.Failure(AppError.Unauthorized)
        val updated = current.copy(
            displayName = displayName ?: current.displayName,
            country = country ?: current.country,
        )
        state.value = AuthState.SignedIn(updated)
        return DataResult.Success(updated)
    }

    override suspend fun publicProfile(id: String): DataResult<PublicProfile> =
        DataResult.Success(demoSeed.publicProfile(id))
}

@Singleton
class FakeStatsRepository @Inject constructor(
    private val solveRepository: SolveRepository,
    private val calculator: SessionStatsCalculator,
    private val demoSeed: DemoSeed,
) : StatsRepository {

    override fun observeStats(event: WcaEvent): Flow<EventStats> =
        solveRepository.observeHistory(event).map { calculator(event, it) }

    override suspend fun leaderboard(
        event: WcaEvent,
        metric: LeaderboardMetric,
        scope: LeaderboardScope,
        cursor: String?,
    ): DataResult<LeaderboardPage> =
        DataResult.Success(demoSeed.leaderboard(event, metric, scope))
}

@Singleton
class FakeProfileRepository @Inject constructor(
    private val demoSeed: DemoSeed,
) : ProfileRepository {

    override suspend fun profile(
        event: WcaEvent,
        rankScope: LeaderboardScope,
    ): DataResult<PlayerProfile> = DataResult.Success(demoSeed.profile(event, rankScope))
}

@Singleton
class FakeRaceRepository @Inject constructor() : RaceRepository {

    override suspend fun createRace(mode: RaceMode, event: WcaEvent): DataResult<RaceRoom> {
        if (mode == RaceMode.QUICK && !event.quickMatch) {
            return DataResult.Failure(
                AppError.Validation("${event.displayName} isn't in quick match — start a private room instead."),
            )
        }
        return DataResult.Success(
            RaceRoom(
                id = "fake-race",
                mode = mode,
                event = event,
                status = RaceStatus.WAITING,
                code = if (mode == RaceMode.PRIVATE) "CUBE42" else null,
            ),
        )
    }

    override suspend fun joinRace(code: String): DataResult<RaceRoom> = DataResult.Success(
        RaceRoom(
            id = "fake-race",
            mode = RaceMode.PRIVATE,
            event = WcaEvent.THREE,
            status = RaceStatus.READY_CHECK,
            code = code.uppercase(),
        ),
    )

    override suspend fun history(): DataResult<List<RaceRoom>> = DataResult.Success(emptyList())
}

/**
 * The in-memory Friends screen. Seeded from [DemoSeed.friends] and **stateful**: accepting an
 * incoming request flips it to accepted in place, and inviting appends a pending outgoing row —
 * so the whole friends flow is demoable end to end with no server.
 */
@Singleton
class FakeFriendsRepository @Inject constructor(
    private val demoSeed: DemoSeed,
) : FriendsRepository {

    private val state = MutableStateFlow(demoSeed.friends())

    override suspend fun friends(): DataResult<List<Friend>> = DataResult.Success(state.value)

    override suspend fun invite(query: String): DataResult<Unit> {
        val q = query.trim()
        if (q.isEmpty()) {
            return DataResult.Failure(AppError.Validation("Enter a username to invite."))
        }
        // Append a pending, outgoing request so the demo shows the row appear.
        state.value = state.value + Friend(
            userId = "invited-${q.lowercase()}",
            displayName = q,
            status = FriendStatus.PENDING,
            incoming = false,
        )
        return DataResult.Success(Unit)
    }

    override suspend fun accept(userId: String): DataResult<Unit> {
        val target = state.value.firstOrNull { it.userId == userId }
            ?: return DataResult.Failure(AppError.NotFound("That request is gone."))
        if (!target.incoming) {
            return DataResult.Failure(AppError.Validation("Only incoming requests can be accepted."))
        }
        state.value = state.value.map {
            if (it.userId == userId) {
                it.copy(status = FriendStatus.ACCEPTED, incoming = false)
            } else {
                it
            }
        }
        return DataResult.Success(Unit)
    }
}

/**
 * The in-memory Tournaments screen. Seeded from [DemoSeed.tournaments] and **stateful**:
 * registering bumps the entrant count and flips [Tournament.registered] in place, and rejects a
 * full or already-registered bracket exactly as the real flow would — so the register flow is
 * demoable end to end. The detail view is served by [DemoSeed.bracket].
 */
@Singleton
class FakeTournamentRepository @Inject constructor(
    private val demoSeed: DemoSeed,
) : TournamentRepository {

    private val state = MutableStateFlow(demoSeed.tournaments())

    override suspend fun tournaments(): DataResult<List<Tournament>> =
        DataResult.Success(state.value)

    override suspend fun tournament(id: String): DataResult<TournamentDetail> {
        val tournament = state.value.firstOrNull { it.id == id }
            ?: return DataResult.Failure(AppError.NotFound("That tournament could not be found."))
        return DataResult.Success(demoSeed.bracket(tournament))
    }

    override suspend fun register(id: String): DataResult<Unit> {
        val current = state.value.firstOrNull { it.id == id }
            ?: return DataResult.Failure(AppError.NotFound("That tournament is gone."))
        if (current.registered) return DataResult.Success(Unit)
        if (current.isFull) {
            return DataResult.Failure(AppError.Validation("This bracket is full."))
        }
        state.value = state.value.map {
            if (it.id == id) it.copy(registered = true, entrants = it.entrants + 1) else it
        }
        return DataResult.Success(Unit)
    }
}
