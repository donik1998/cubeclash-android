package com.donik1998.cubeclash.core.data.mapper

import android.util.Log
import com.donik1998.cubeclash.core.model.Tournament
import com.donik1998.cubeclash.core.model.TournamentDetail
import com.donik1998.cubeclash.core.model.TournamentMatch
import com.donik1998.cubeclash.core.model.TournamentRound
import com.donik1998.cubeclash.core.model.TournamentStatus
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.TournamentDetailResponseDto
import com.donik1998.cubeclash.core.network.dto.TournamentDto
import com.donik1998.cubeclash.core.network.dto.TournamentListResponseDto
import com.donik1998.cubeclash.core.network.dto.TournamentMatchDto
import com.donik1998.cubeclash.core.network.dto.TournamentRoundDto
import java.time.Instant

private const val TAG = "TournamentMapper"

/**
 * The border between the wire and the domain for tournaments.
 *
 * ⚠️ These endpoints are unimplemented server-side (404). The shapes come from the API Design doc
 * and the Flutter client — and `GET /tournaments/{id}` is not even in the roadmap's five endpoints,
 * only inferred from the client — so all of this must be re-verified once the routes exist.
 *
 * The server is not trustworthy about shape, so every DTO field is nullable and this mapper is the
 * single place that decides what "missing" means:
 *
 *  - **`id` and `name` are load-bearing.** A card with no identity is un-renderable, so a
 *    missing/blank one **drops the card** — logged, never fabricated.
 *  - **`event`** maps through [WcaEvent.fromId], which is lenient (an unknown id degrades to 3×3).
 *  - **`status`** maps through [TournamentStatus.fromWire]; unknown/missing → [TournamentStatus.UNKNOWN].
 *  - **`entrants` / `capacity`** default to **0** — a neutral count. `is_full` is never read from
 *    the wire; it is derived on the model from these two.
 *  - **`starts_at`** is parsed leniently and **stays null** when missing or unparseable; the UI
 *    shows "time TBD". Fabricating epoch (1970) would be a claim rather than a blank.
 *  - **`description`** defaults to blank; **`registered`** defaults to false.
 *
 * One malformed element never blanks the list.
 */
fun TournamentDto.toDomain(): Tournament? {
    val cleanId = id?.trim().orEmpty()
    val cleanName = name?.trim().orEmpty()
    if (cleanId.isEmpty() || cleanName.isEmpty()) {
        Log.w(TAG, "Dropping tournament: id=$id name=$name")
        return null
    }
    return Tournament(
        id = cleanId,
        name = cleanName,
        event = WcaEvent.fromId(event),
        status = TournamentStatus.fromWire(status),
        entrants = entrants ?: 0,
        capacity = capacity ?: 0,
        startsAt = startsAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
        description = description?.trim().orEmpty(),
        registered = registered ?: false,
    )
}

fun TournamentListResponseDto.toDomain(): List<Tournament> =
    items.orEmpty().mapNotNull { it?.toDomain() }

/**
 * The detail response maps to a [TournamentDetail] only when its tournament header has a usable
 * identity; a header that fails the drop rules yields null (the repository turns that into a
 * failure). A single un-renderable round or match is dropped rather than dropping the whole detail.
 */
fun TournamentDetailResponseDto.toDomain(): TournamentDetail? {
    val header = tournament?.toDomain()
    if (header == null) {
        Log.w(TAG, "Dropping tournament detail: header had no usable identity")
        return null
    }
    return TournamentDetail(
        tournament = header,
        rounds = rounds.orEmpty().mapNotNull { it?.toDomain() },
    )
}

/** A round with no name is un-renderable and dropped; empty matches are fine (bracket not drawn). */
private fun TournamentRoundDto.toDomain(): TournamentRound? {
    val cleanName = name?.trim().orEmpty()
    if (cleanName.isEmpty()) {
        Log.w(TAG, "Dropping tournament round: no name")
        return null
    }
    return TournamentRound(
        name = cleanName,
        matches = matches.orEmpty().mapNotNull { it?.toDomain() },
    )
}

/** A match needs both seats to be rendered; one missing drops just that match. */
private fun TournamentMatchDto.toDomain(): TournamentMatch? {
    val a = playerA?.trim().orEmpty()
    val b = playerB?.trim().orEmpty()
    if (a.isEmpty() || b.isEmpty()) {
        Log.w(TAG, "Dropping tournament match: player_a=$playerA player_b=$playerB")
        return null
    }
    return TournamentMatch(
        playerA = a,
        playerB = b,
        timeAMs = timeAMs,
        timeBMs = timeBMs,
        winner = winner?.trim()?.takeIf { it.isNotEmpty() },
    )
}
