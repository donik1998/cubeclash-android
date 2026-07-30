package com.donik1998.cubeclash.core.data.mapper

import android.util.Log
import com.donik1998.cubeclash.core.model.LeaderboardEntry
import com.donik1998.cubeclash.core.model.LeaderboardPage
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.LeaderboardItemDto
import com.donik1998.cubeclash.core.network.dto.LeaderboardResponseDto
import java.time.Instant

private const val TAG = "LeaderboardMapper"

/**
 * The border between the wire and the domain for leaderboards.
 *
 * The server is not trustworthy about shape, so every DTO field is nullable and this mapper is
 * the single place that decides what "missing" means for each one:
 *
 *  - **`rank`, `user_id`, `value_ms`, `display_name` are load-bearing.** A row missing any of
 *    them is meaningless on a leaderboard, so the row is **dropped** — logged and (where
 *    analytics exist) counted, never fabricated. A rank of 0 or a nameless entry would be a
 *    claim rather than a blank.
 *  - **`country` is optional in the domain too.** A cuber with no country on file is still
 *    rankable; the row simply renders without a country line, so it **stays nullable**.
 *  - **`event`** defaults to the requested event when absent — every row on a `?event=3x3`
 *    page is a 3×3 row, so a missing echo is neutral, not load-bearing.
 *  - **`solved_at`** is parsed leniently and **stays nullable**: a missing or unparseable
 *    timestamp is not worth dropping a ranked row over, and fabricating [Instant.EPOCH] (1970)
 *    would be a claim rather than a blank — so it maps to `null`, matching iOS and Flutter.
 *
 * One malformed element never blanks the list — [toDomain] on the response maps each item,
 * drops the nulls, and keeps the rest.
 */
fun LeaderboardItemDto.toDomain(requestedEvent: WcaEvent): LeaderboardEntry? {
    val rank = rank
    val userId = userId
    val displayName = displayName
    val valueMs = valueMs
    if (rank == null || userId.isNullOrBlank() || displayName.isNullOrBlank() || valueMs == null) {
        Log.w(
            TAG,
            "Dropping leaderboard row: rank=$rank user_id=$userId " +
                "display_name=$displayName value_ms=$valueMs",
        )
        return null
    }
    return LeaderboardEntry(
        rank = rank,
        userId = userId,
        displayName = displayName,
        country = country?.trim()?.takeIf { it.isNotEmpty() },
        valueMs = valueMs,
        event = event?.let { WcaEvent.fromId(it) } ?: requestedEvent,
        solvedAt = solvedAt?.let { runCatching { Instant.parse(it) }.getOrNull() },
    )
}

fun LeaderboardResponseDto.toDomain(requestedEvent: WcaEvent): LeaderboardPage = LeaderboardPage(
    entries = items.orEmpty().mapNotNull { it?.toDomain(requestedEvent) },
    nextCursor = nextCursor,
    viewer = viewer?.toDomain(requestedEvent),
)
