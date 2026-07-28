package com.donik1998.cubeclash.core.data.repository

import com.donik1998.cubeclash.core.data.mapper.toDomain
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.repository.RaceRepository
import com.donik1998.cubeclash.core.domain.repository.ScrambleRepository
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.domain.repository.StatsRepository
import com.donik1998.cubeclash.core.domain.scramble.ScrambleGenerator
import com.donik1998.cubeclash.core.domain.stats.SessionStatsCalculator
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.LeaderboardMetric
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.RaceMode
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.ApiErrorMapper
import com.donik1998.cubeclash.core.network.CubeClashApi
import com.donik1998.cubeclash.core.network.call
import com.donik1998.cubeclash.core.network.dto.CreateRaceRequest
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
    ): DataResult<List<LeaderboardEntry>> =
        errors.call {
            api.leaderboard(event.id, metric.wire, scope.wire).items.map { it.toDomain() }
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
