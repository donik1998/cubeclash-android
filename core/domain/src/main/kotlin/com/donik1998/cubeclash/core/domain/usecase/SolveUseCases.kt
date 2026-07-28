package com.donik1998.cubeclash.core.domain.usecase

import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.common.TimeSource
import com.donik1998.cubeclash.core.domain.repository.ScrambleRepository
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.domain.stats.SessionStatsCalculator
import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.SyncState
import com.donik1998.cubeclash.core.model.WcaEvent
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Records an attempt.
 *
 * The `clientId` minted here is the idempotency key the server upserts on, so this is the
 * one place a solve gets its identity — and the reason a retry after a dropped connection
 * cannot produce two rows.
 */
class LogSolveUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
    private val timeSource: TimeSource,
) {
    suspend operator fun invoke(
        event: WcaEvent,
        scramble: Scramble,
        scrambleSource: ScrambleSource,
        timeMs: Long,
        penalty: Penalty = Penalty.NONE,
        moveCount: Int? = null,
        solvedCount: Int? = null,
        attemptedCount: Int? = null,
    ): DataResult<Solve> {
        val clientId = UUID.randomUUID().toString()
        val solve = Solve(
            id = clientId,
            clientId = clientId,
            event = event,
            scramble = scramble,
            scrambleSource = scrambleSource,
            timeMs = timeMs,
            penalty = penalty,
            solvedAt = Instant.ofEpochMilli(timeSource.nowMillis()),
            moveCount = moveCount,
            solvedCount = solvedCount,
            attemptedCount = attemptedCount,
            syncState = SyncState.PENDING,
        )
        return solveRepository.logSolve(solve)
    }
}

class ObserveSessionUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
) {
    operator fun invoke(event: WcaEvent): Flow<List<Solve>> = solveRepository.observeSession(event)
}

class ObserveSessionStatsUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
    private val calculator: SessionStatsCalculator,
) {
    operator fun invoke(event: WcaEvent): Flow<EventStats> =
        solveRepository.observeSession(event).map { calculator(event, it) }
}

class UpdatePenaltyUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
) {
    suspend operator fun invoke(solveId: String, penalty: Penalty): DataResult<Solve> =
        solveRepository.updatePenalty(solveId, penalty)
}

class DeleteSolveUseCase @Inject constructor(
    private val solveRepository: SolveRepository,
) {
    suspend operator fun invoke(solveId: String): DataResult<Unit> =
        solveRepository.deleteSolve(solveId)
}

class NextScrambleUseCase @Inject constructor(
    private val scrambleRepository: ScrambleRepository,
) {
    suspend operator fun invoke(event: WcaEvent, cubeCount: Int = 3): DataResult<Scramble> =
        scrambleRepository.next(event, cubeCount)
}
