package com.donik1998.cubeclash.core.data.repository

import com.donik1998.cubeclash.core.data.mapper.toDomain
import com.donik1998.cubeclash.core.data.mapper.toEntity
import com.donik1998.cubeclash.core.data.mapper.toRequest
import com.donik1998.cubeclash.core.database.SolveDao
import com.donik1998.cubeclash.core.domain.common.DataResult
import com.donik1998.cubeclash.core.domain.common.TimeSource
import com.donik1998.cubeclash.core.domain.repository.SolveRepository
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.SyncState
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.ApiErrorMapper
import com.donik1998.cubeclash.core.network.CubeClashApi
import com.donik1998.cubeclash.core.network.call
import com.donik1998.cubeclash.core.network.dto.SyncRequest
import com.donik1998.cubeclash.core.network.dto.UpdatePenaltyRequest
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * **Local-first.** A solve is written to the device and shown immediately; the network call is
 * a follow-up that flips `sync_state`, never a gate on the UI.
 *
 * That ordering is the whole offline story: a timer that refuses to record a solve because a
 * train went into a tunnel is a broken timer, and the `client_id` idempotency key means the
 * eventual retry cannot duplicate anything.
 */
@Singleton
class SolveRepositoryImpl @Inject constructor(
    private val dao: SolveDao,
    private val api: CubeClashApi,
    private val errors: ApiErrorMapper,
    private val timeSource: TimeSource,
) : SolveRepository {

    override fun observeSession(event: WcaEvent): Flow<List<Solve>> =
        dao.observeSession(event.id).map { entities -> entities.map { it.toDomain() } }

    override fun observeHistory(event: WcaEvent): Flow<List<Solve>> =
        dao.observeByEvent(event.id).map { entities -> entities.map { it.toDomain() } }

    override suspend fun logSolve(solve: Solve): DataResult<Solve> {
        dao.upsert(solve.toEntity())

        return errors.call { api.createSolve(solve.toRequest()) }
            .let { result ->
                when (result) {
                    is DataResult.Success -> {
                        val synced = result.data.toDomain(SyncState.SYNCED)
                        dao.upsert(synced.toEntity())
                        DataResult.Success(synced)
                    }
                    // The solve is already on disk and already on screen. A failed push is a
                    // sync problem, not a lost attempt.
                    is DataResult.Failure -> DataResult.Success(solve)
                }
            }
    }

    override suspend fun updatePenalty(solveId: String, penalty: Penalty): DataResult<Solve> {
        dao.updatePenalty(solveId, penalty.wire)
        val local = dao.byClientId(solveId)?.toDomain()
            ?: return DataResult.Failure(
                com.donik1998.cubeclash.core.domain.common.AppError.NotFound("That solve is gone."),
            )

        return errors.call { api.updatePenalty(local.id, UpdatePenaltyRequest(penalty.wire)) }
            .let { result ->
                when (result) {
                    is DataResult.Success -> {
                        val synced = result.data.toDomain(SyncState.SYNCED)
                        dao.upsert(synced.toEntity())
                        DataResult.Success(synced)
                    }

                    is DataResult.Failure -> DataResult.Success(local)
                }
            }
    }

    override suspend fun deleteSolve(solveId: String): DataResult<Unit> {
        // Soft delete, so the deletion can be synced rather than silently vanishing.
        dao.softDelete(solveId, timeSource.nowMillis())
        return errors.call { api.deleteSolve(solveId) }
    }

    override suspend fun clearSession(event: WcaEvent): DataResult<Unit> {
        dao.clearEvent(event.id)
        return DataResult.Success(Unit)
    }

    override suspend fun sync(): DataResult<Int> {
        val pending = dao.pending()
        if (pending.isEmpty()) return DataResult.Success(0)

        return errors.call {
            api.sync(SyncRequest(solves = pending.map { it.toDomain().toRequest() }))
        }.let { result ->
            when (result) {
                is DataResult.Success -> {
                    result.data.changes.forEach { dao.upsert(it.toDomain(SyncState.SYNCED).toEntity()) }
                    DataResult.Success(result.data.applied)
                }

                is DataResult.Failure -> result
            }
        }
    }
}
