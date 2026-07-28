package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.model.EventStats
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.Penalty
import com.donik1998.cubeclash.core.model.RaceMode
import com.donik1998.cubeclash.core.model.RacePlayer
import com.donik1998.cubeclash.core.model.RaceRoom
import com.donik1998.cubeclash.core.model.RaceStatus
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.User
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.LeaderboardEntryDto
import com.donik1998.cubeclash.core.network.dto.RaceDto
import com.donik1998.cubeclash.core.network.dto.RacePlayerDto
import com.donik1998.cubeclash.core.network.dto.StatsDto
import com.donik1998.cubeclash.core.network.dto.UserDto

fun UserDto.toDomain(): User = User(
    id = id,
    displayName = displayName,
    email = email,
    country = country,
    avatarUrl = avatarUrl,
    elo = elo,
    solveCount = solveCount,
)

fun StatsDto.toDomain(): EventStats {
    val wcaEvent = WcaEvent.fromId(event)
    return EventStats(
        event = wcaEvent,
        best = best,
        ao5 = ao5,
        ao12 = ao12,
        ao100 = ao100,
        mo3 = mo3,
        solveCount = solveCount,
    )
}

fun LeaderboardEntryDto.toDomain(): LeaderboardEntry = LeaderboardEntry(
    rank = rank,
    user = user.toDomain(),
    value = value,
    isCurrentUser = isCurrentUser,
)

fun RacePlayerDto.toDomain(): RacePlayer = RacePlayer(
    userId = userId,
    displayName = displayName,
    country = country,
    avatarUrl = avatarUrl,
    elo = elo,
    isReady = ready,
    finalTimeMs = timeMs,
    penalty = Penalty.fromWire(penalty),
)

fun RaceDto.toDomain(): RaceRoom = RaceRoom(
    id = raceId,
    mode = if (mode == RaceMode.PRIVATE.wire) RaceMode.PRIVATE else RaceMode.QUICK,
    event = WcaEvent.fromId(event),
    status = RaceStatus.fromWire(status),
    code = code,
    players = players.map { it.toDomain() },
    scramble = scramble?.let { Scramble.parse(it, ScrambleNotation.FACE_TURN) } ?: Scramble.EMPTY,
)
