package com.donik1998.cubeclash.core.testing

import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.ScrambleSource
import com.donik1998.cubeclash.core.model.Solve
import com.donik1998.cubeclash.core.model.WcaEvent
import java.time.Instant

object TestSolves {

    val scramble = Scramble.parse("R U R' U' F2 L D2", ScrambleNotation.FACE_TURN)

    fun solve(
        id: String = "solve-1",
        event: WcaEvent = WcaEvent.THREE,
        timeMs: Long = 11_240,
        penalty: Penalty = Penalty.NONE,
    ) = Solve(
        id = id,
        clientId = id,
        event = event,
        scramble = scramble,
        scrambleSource = ScrambleSource.RANDOM,
        timeMs = timeMs,
        penalty = penalty,
        solvedAt = Instant.ofEpochMilli(1_785_000_000_000),
    )

    fun session(times: List<Long>, event: WcaEvent = WcaEvent.THREE): List<Solve> =
        times.mapIndexed { index, time -> solve(id = "solve-$index", event = event, timeMs = time) }
}
