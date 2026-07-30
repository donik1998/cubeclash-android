package com.donik1998.cubeclash.core.data.mapper

import android.util.Log
import com.donik1998.cubeclash.core.model.HeadToHead
import com.donik1998.cubeclash.core.model.PublicProfile
import com.donik1998.cubeclash.core.network.dto.HeadToHeadDto
import com.donik1998.cubeclash.core.network.dto.PublicUserDto
import com.donik1998.cubeclash.core.network.dto.PublicUserResponseDto

private const val TAG = "PublicProfileMapper"

/**
 * The border between the wire and the domain for `GET /users/:id`, a public player profile.
 *
 * The server is not trustworthy about shape, so this mapper is the single place that decides what
 * "missing" means for each field. Never throws.
 *
 *  - **`user` and `user.id` are load-bearing.** Without an identity the profile is un-renderable,
 *    so a missing/blank one **drops the profile** — this returns null, logged, and the repository
 *    turns that into a `DataResult.Failure`. No fabricated id.
 *  - **`display_name`** falls back to blank only after id has cleared; a blank handle is not
 *    identity, so it also **drops** the profile (a nameless player has nothing to render).
 *  - **`country`** is optional in the domain too — a cuber with no country still has a profile,
 *    the subtitle just drops its country segment — so it **stays null**.
 *  - **`elo`** defaults to **1000** when absent (the server default).
 *  - **`best_single_ms` / `ao5` / `ao12`** (best-ever singles/averages) **stay null**; the tile
 *    renders an em dash. A fabricated 0 would be a claim of a sub-millisecond solve rather than a
 *    blank.
 *  - **`head_to_head`** **stays null** when the object is absent — null means *never raced*, which
 *    is a distinct UI state from a present `0-0`. When the object *is* present, its `wins`/`losses`
 *    default to 0 (its very presence already establishes that they have raced).
 */
fun PublicUserResponseDto.toDomain(): PublicProfile? {
    val user = user
    if (user == null) {
        Log.w(TAG, "Dropping public profile: no user object")
        return null
    }
    return user.toDomain()
}

fun PublicUserDto.toDomain(): PublicProfile? {
    val cleanId = id.trim()
    val cleanName = displayName?.trim().orEmpty()
    if (cleanId.isEmpty() || cleanName.isEmpty()) {
        Log.w(TAG, "Dropping public profile: id=$id display_name=$displayName")
        return null
    }
    return PublicProfile(
        id = cleanId,
        displayName = cleanName,
        country = country?.trim()?.takeIf { it.isNotEmpty() },
        elo = elo ?: DEFAULT_ELO,
        bestSingleMs = bestSingleMs,
        bestAo5Ms = ao5,
        bestAo12Ms = ao12,
        headToHead = headToHead?.toDomain(),
    )
}

/**
 * A present `head_to_head` object always yields a [HeadToHead]; the object's presence *is* the
 * "they have raced" signal, so a missing count on either side collapses to 0 rather than dropping.
 */
private fun HeadToHeadDto.toDomain(): HeadToHead = HeadToHead(
    wins = wins ?: 0,
    losses = losses ?: 0,
)

private const val DEFAULT_ELO = 1000
