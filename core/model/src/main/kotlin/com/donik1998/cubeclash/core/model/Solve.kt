package com.donik1998.cubeclash.core.model

import java.time.Instant

/**
 * One attempt.
 *
 * [clientId] is generated on the device and is the idempotency key for `POST /solves`:
 * the server upserts on `(user_id, client_id)`, so a retry after a flaky network can
 * never duplicate a solve.
 */
data class Solve(
    val id: String,
    val clientId: String,
    val event: WcaEvent,
    val scramble: Scramble,
    val scrambleSource: ScrambleSource,
    /** The attempt's duration, for every event. Not necessarily what it is ranked on. */
    val timeMs: Long,
    val penalty: Penalty,
    val solvedAt: Instant,
    /** Fewest Moves only; omitted from the wire entirely for the other sixteen. */
    val moveCount: Int? = null,
    /** Multi-Blind only. */
    val solvedCount: Int? = null,
    /** Multi-Blind only. */
    val attemptedCount: Int? = null,
    /** Server-owned: the client never decides what a personal best is. */
    val isPb: Boolean = false,
    val syncState: SyncState = SyncState.SYNCED,
) {
    val result: SolveResult
        get() = when (event.resultKind) {
            ResultKind.TIME -> SolveResult.Timed(timeMs, penalty)
            ResultKind.MOVE_COUNT ->
                SolveResult.MoveCount(moveCount.takeIf { penalty != Penalty.DNF })
            ResultKind.MULTI_BLIND -> SolveResult.MultiBlind(
                solvedCount = if (penalty == Penalty.DNF) 0 else (solvedCount ?: 0),
                attemptedCount = attemptedCount ?: 0,
                timeMs = timeMs,
            )
        }

    val isDnf: Boolean get() = result.isDnf
}

/** Local-first writes carry their sync state so the UI can be honest about what is on the server. */
enum class SyncState { PENDING, SYNCED, FAILED }
