package com.donik1998.cubeclash.core.data.mapper

import android.util.Log
import com.donik1998.cubeclash.core.model.LeaderboardScope
import com.donik1998.cubeclash.core.model.PlayerProfile
import com.donik1998.cubeclash.core.model.ProfileRank
import com.donik1998.cubeclash.core.model.WcaEvent
import com.donik1998.cubeclash.core.network.dto.ProfileRankDto
import com.donik1998.cubeclash.core.network.dto.ProfileResponseDto

private const val TAG = "ProfileMapper"

/**
 * The border between the wire and the domain for the You · Profile aggregate.
 *
 * The server is not trustworthy about shape, so every DTO field is nullable and this mapper is
 * the single place that decides what "missing" means for each one (spec §6):
 *
 *  - **`user`, `user.id`, `user.display_name` are load-bearing.** Without an identity the whole
 *    profile is un-renderable, so a missing/blank one **drops the profile** — this returns null,
 *    logged, and the real repository turns that into a `DataResult.Failure`. There is no
 *    fabricated id or blank handle.
 *  - **`country`** is optional in the domain too — a cuber with no country still has a profile,
 *    the subtitle simply drops its country segment — so it **stays null**.
 *  - **`elo`** defaults to **1000** when absent (the profile default per §6/§9; note the general
 *    `User` model defaults to 1200, but the profile aggregate uses 1000).
 *  - **`rank`** drops to null if the object is absent or its `position` is null — the "#…"
 *    segment then disappears. When present, its `event`/`metric`/`scope` default to the
 *    requested event / `"single"` / the requested scope.
 *  - **`best_single_ms`** and **`win_rate`** stay null (the tile renders "—"); a fabricated 0
 *    would be a claim rather than a blank. `best_single_event` defaults to the requested event.
 *  - **`total_solves`, `wins`, `losses`, `friend_count`** default to **0** — a genuinely neutral
 *    "none yet" that reads identically to absent.
 */
fun ProfileResponseDto.toDomain(
    requestedEvent: WcaEvent,
    requestedScope: LeaderboardScope,
): PlayerProfile? {
    val user = user
    val id = user?.id
    val displayName = user?.displayName
    if (user == null || id.isNullOrBlank() || displayName.isNullOrBlank()) {
        Log.w(TAG, "Dropping profile: user=$user id=$id display_name=$displayName")
        return null
    }

    return PlayerProfile(
        id = id,
        displayName = displayName,
        country = user.country?.trim()?.takeIf { it.isNotEmpty() },
        elo = user.elo ?: DEFAULT_ELO,
        rank = rank?.toDomain(requestedEvent, requestedScope),
        bestSingleMs = stats?.bestSingleMs,
        bestSingleEvent = stats?.bestSingleEvent?.let { WcaEvent.fromId(it) } ?: requestedEvent,
        totalSolves = stats?.totalSolves ?: 0,
        winRate = stats?.winRate,
        wins = stats?.wins ?: 0,
        losses = stats?.losses ?: 0,
        friendCount = friendCount ?: 0,
    )
}

/**
 * A rank object with no [ProfileRankDto.position] is meaningless — the "#…" segment is dropped —
 * so this returns null and the caller stores a null rank. When present, the tuple fields default
 * to what the request asked for so the client never guesses.
 */
private fun ProfileRankDto.toDomain(
    requestedEvent: WcaEvent,
    requestedScope: LeaderboardScope,
): ProfileRank? {
    val position = position ?: return null
    return ProfileRank(
        position = position,
        event = event?.let { WcaEvent.fromId(it) } ?: requestedEvent,
        metric = metric ?: "single",
        scope = scope?.let { wire -> LeaderboardScope.entries.firstOrNull { it.wire == wire } }
            ?: requestedScope,
    )
}

private const val DEFAULT_ELO = 1000
