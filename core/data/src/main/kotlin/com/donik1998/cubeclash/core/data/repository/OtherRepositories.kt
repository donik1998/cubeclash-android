package com.donik1998.cubeclash.core.data.repository

import com.donik1998.cubeclash.core.data.mapper.toDomain
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.FriendsRepository
import com.donik1998.cubeclash.core.domain.repository.ProfileRepository
import com.donik1998.cubeclash.core.domain.repository.RaceRepository
import com.donik1998.cubeclash.core.domain.repository.ScrambleRepository
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.domain.repository.StatsRepository
import com.donik1998.cubeclash.core.domain.repository.TournamentRepository
import com.donik1998.cubeclash.core.domain.scramble.ScrambleGenerator
import com.donik1998.cubeclash.core.domain.stats.SessionStatsCalculator
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardPage
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.RaceMode
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentDetail
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.ApiErrorMapper
import com.donik1998.cubeclash.core.network.CubeClashApi
import com.donik1998.cubeclash.core.network.call
import com.donik1998.cubeclash.core.network.dto.CreateRaceRequest
import com.donik1998.cubeclash.core.network.dto.FriendInviteRequest
import com.donik1998.cubeclash.core.network.dto.JoinRaceRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Scrambles are generated on-device.
 *
 * `GET /scramble` exists but is not on the critical path — a timer that cannot propose a
 * scramble without a round trip is a timer that stops working on a train.
 */
@Singleton
class LocalScrambleRepository @Inject constructor(
    private val generator: ScrambleGenerator,
) : ScrambleRepository {
    override suspend fun next(event: WcaEvent, cubeCount: Int): DataResult<Scramble> =
        DataResult.Success(generator.generate(event, cubeCount))
}

/**
 * Session statistics are computed locally from the local-first session; `GET /stats` is the
 * server's own aggregate over *all* solves and is a different question.
 */
@Singleton
class StatsRepositoryImpl @Inject constructor(
    private val solveRepository: SolveRepository,
    private val calculator: SessionStatsCalculator,
    private val api: CubeClashApi,
    private val errors: ApiErrorMapper,
) : StatsRepository {

    override fun observeStats(event: WcaEvent): Flow<EventStats> =
        solveRepository.observeHistory(event).map { calculator(event, it) }

    override suspend fun leaderboard(
        event: WcaEvent,
        metric: LeaderboardMetric,
        scope: LeaderboardScope,
        cursor: String?,
    ): DataResult<LeaderboardPage> =
        errors.call {
            api.leaderboard(event.id, metric.wire, scope.wire, cursor).toDomain(event)
        }
}

/**
 * The You · Profile aggregate over `GET /me/profile`. The mapper returns null when the payload
 * has no usable identity; that null becomes a [DataResult.Failure] here rather than a success
 * carrying a blank profile — the screen would have nothing to render either way, and a failure
 * lets it show its retry affordance.
 */
@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: CubeClashApi,
    private val errors: ApiErrorMapper,
) : ProfileRepository {

    override suspend fun profile(
        event: WcaEvent,
        rankScope: LeaderboardScope,
    ): DataResult<PlayerProfile> =
        errors.call {
            api.profile(event.id, rankScope.wire).toDomain(event, rankScope)
                ?: throw IllegalStateException("Profile response had no usable user.")
        }
}

@Singleton
class RaceRepositoryImpl @Inject constructor(
    private val api: CubeClashApi,
    private val errors: ApiErrorMapper,
) : RaceRepository {

    override suspend fun createRace(mode: RaceMode, event: WcaEvent): DataResult<RaceRoom> {
        // Reject out-of-set events here rather than queueing a match that can never fill.
        if (mode == RaceMode.QUICK && !event.quickMatch) {
            return DataResult.Failure(
                com.donik1998.cubeclash.core.domain.common.AppError.Validation(
                    "${event.displayName} isn't in quick match — start a private room instead.",
                ),
            )
        }
        if (!event.raceable) {
            return DataResult.Failure(
                com.donik1998.cubeclash.core.domain.common.AppError.Validation(
                    "${event.displayName} can't be raced.",
                ),
            )
        }
        return errors.call { api.createRace(CreateRaceRequest(mode.wire, event.id)).toDomain() }
    }

    override suspend fun joinRace(code: String): DataResult<RaceRoom> =
        errors.call { api.joinRace(JoinRaceRequest(code.trim().uppercase())).toDomain() }

    override suspend fun history(): DataResult<List<RaceRoom>> =
        errors.call { api.raceHistory().items.map { it.toDomain() } }
}

/**
 * The friends list and invite/accept flow over `GET /friends` and friends.
 *
 * ⚠️ **These routes do not exist on `cubeclash-backend` yet** — the `friends` module is a bare
 * `.module.ts`, so every call here 404s today. The shape is taken from the vault's API Design doc
 * and the Flutter client, NOT an observed response, and must be re-verified once the routes exist.
 * With `cubeclash.useFakeData=true` (the default build) this class is never constructed and
 * `FakeFriendsRepository` runs instead.
 */
@Singleton
class FriendsRepositoryImpl @Inject constructor(
    private val api: CubeClashApi,
    private val errors: ApiErrorMapper,
) : FriendsRepository {

    override suspend fun friends(): DataResult<List<Friend>> =
        errors.call { api.friends().toDomain() }

    override suspend fun invite(query: String): DataResult<Unit> =
        errors.call { api.inviteFriend(FriendInviteRequest(query.trim())) }

    override suspend fun accept(userId: String): DataResult<Unit> =
        errors.call { api.acceptFriend(userId) }
}

/**
 * The tournament lobby, bracket detail and registration.
 *
 * ⚠️ **These routes do not exist on `cubeclash-backend` yet** — the `tournaments` module is a bare
 * `.module.ts`, so every call here 404s. `GET /tournaments/{id}` is not even in the roadmap's five
 * endpoints (inferred from the Flutter client), so it is doubly unverified. Re-verify all shapes
 * once the routes exist. With `cubeclash.useFakeData=true` this class is never constructed and
 * `FakeTournamentRepository` runs instead.
 */
@Singleton
class TournamentRepositoryImpl @Inject constructor(
    private val api: CubeClashApi,
    private val errors: ApiErrorMapper,
) : TournamentRepository {

    override suspend fun tournaments(): DataResult<List<Tournament>> =
        errors.call { api.tournaments().toDomain() }

    override suspend fun tournament(id: String): DataResult<TournamentDetail> =
        errors.call {
            api.tournament(id).toDomain()
                ?: throw IllegalStateException("Tournament detail had no usable tournament.")
        }

    override suspend fun register(id: String): DataResult<Unit> =
        errors.call { api.registerForTournament(id) }
}
