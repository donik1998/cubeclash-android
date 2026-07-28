package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.database.SolveEntity
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.ResultKind
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.SyncState
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.CreateSolveRequest
import com.donik1998.cubeclash.core.network.dto.SolveDto
import java.time.Instant

/**
 * The border between the wire, the disk and the domain.
 *
 * Two rules live here and nowhere else:
 *  - the scramble is a plain newline-separated string on the wire and in the database, and
 *    `\n` must survive both trips;
 *  - the three long-form columns are **omitted** for the fifteen events they do not apply to,
 *    because an absent key says *inapplicable* where an explicit null does not.
 */
fun SolveDto.toDomain(syncState: SyncState = SyncState.SYNCED): Solve {
    val wcaEvent = WcaEvent.fromId(event)
    return Solve(
        id = id,
        clientId = clientId,
        event = wcaEvent,
        scramble = Scramble.parse(scramble, wcaEvent.notation),
        scrambleSource = ScrambleSource.fromWire(scrambleSource),
        timeMs = timeMs,
        penalty = Penalty.fromWire(penalty),
        solvedAt = runCatching { Instant.parse(solvedAt) }.getOrElse { Instant.EPOCH },
        moveCount = moveCount,
        solvedCount = solvedCount,
        attemptedCount = attemptedCount,
        isPb = isPb,
        syncState = syncState,
    )
}

fun Solve.toRequest(): CreateSolveRequest = CreateSolveRequest(
    event = event.id,
    scramble = scramble.toWire(),
    scrambleSource = scrambleSource.wire,
    timeMs = timeMs,
    penalty = penalty.wire,
    solvedAt = solvedAt.toString(),
    clientId = clientId,
    moveCount = moveCount.takeIf { event.resultKind == ResultKind.MOVE_COUNT },
    solvedCount = solvedCount.takeIf { event.resultKind == ResultKind.MULTI_BLIND },
    attemptedCount = attemptedCount.takeIf { event.resultKind == ResultKind.MULTI_BLIND },
)

fun Solve.toEntity(): SolveEntity = SolveEntity(
    clientId = clientId,
    serverId = id.takeIf { it != clientId },
    event = event.id,
    scramble = scramble.toWire(),
    scrambleSource = scrambleSource.wire,
    timeMs = timeMs,
    penalty = penalty.wire,
    solvedAt = solvedAt.toEpochMilli(),
    moveCount = moveCount,
    solvedCount = solvedCount,
    attemptedCount = attemptedCount,
    isPb = isPb,
    syncState = syncState.name,
)

fun SolveEntity.toDomain(): Solve {
    val wcaEvent = WcaEvent.fromId(event)
    return Solve(
        id = serverId ?: clientId,
        clientId = clientId,
        event = wcaEvent,
        scramble = Scramble.parse(scramble, wcaEvent.notation),
        scrambleSource = ScrambleSource.fromWire(scrambleSource),
        timeMs = timeMs,
        penalty = Penalty.fromWire(penalty),
        solvedAt = Instant.ofEpochMilli(solvedAt),
        moveCount = moveCount,
        solvedCount = solvedCount,
        attemptedCount = attemptedCount,
        isPb = isPb,
        syncState = runCatching { SyncState.valueOf(syncState) }.getOrDefault(SyncState.PENDING),
    )
}
