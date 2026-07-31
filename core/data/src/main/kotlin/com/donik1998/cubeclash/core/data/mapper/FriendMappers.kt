package com.donik1998.cubeclash.core.data.mapper

import android.util.Log
import com.donik1998.cubeclash.core.model.Friend
import com.donik1998.cubeclash.core.model.FriendStatus
import com.donik1998.cubeclash.core.network.dto.FriendDto
import com.donik1998.cubeclash.core.network.dto.FriendListResponseDto

private const val TAG = "FriendMapper"

/**
 * The border between the wire and the domain for `GET /friends`.
 *
 * ⚠️ The endpoint is unimplemented server-side (404) — this maps the shape from the API Design doc
 * and the Flutter client, not an observed response, and must be re-verified once the route exists.
 *
 * The server is not trustworthy about shape, so every DTO field is nullable and this mapper is the
 * single place that decides what "missing" means for each one:
 *
 *  - **`user_id` and `display_name` are load-bearing.** A friend row with no identity is
 *    un-renderable, so a missing/blank one **drops the row** — logged, never fabricated.
 *  - **`status`** maps through [FriendStatus.fromWire]; an unknown or missing value becomes
 *    [FriendStatus.UNKNOWN], never silently [FriendStatus.ACCEPTED].
 *  - **`country`** is optional in the domain too — a friend with no country still renders, minus
 *    the country segment — so it **stays null**.
 *  - **`avatar_url`** **stays null**; the UI falls back to an initials placeholder.
 *  - **`best_single_ms`** **stays null**; the stat renders an em dash. A fabricated 0 would be a
 *    claim of a sub-millisecond solve.
 *  - **`incoming`** defaults to **false** — a missing flag is a plain "not an incoming request",
 *    which is the safe default (it withholds the Accept action rather than offering it wrongly).
 *
 * One malformed element never blanks the list — [toDomain] on the response maps each item, drops
 * the nulls, and keeps the rest.
 */
fun FriendDto.toDomain(): Friend? {
    val cleanId = userId?.trim().orEmpty()
    val cleanName = displayName?.trim().orEmpty()
    if (cleanId.isEmpty() || cleanName.isEmpty()) {
        Log.w(TAG, "Dropping friend row: user_id=$userId display_name=$displayName")
        return null
    }
    return Friend(
        userId = cleanId,
        displayName = cleanName,
        status = FriendStatus.fromWire(status),
        countryCode = country?.trim()?.takeIf { it.isNotEmpty() },
        avatarUrl = avatarUrl?.trim()?.takeIf { it.isNotEmpty() },
        bestSingleMs = bestSingleMs,
        incoming = incoming ?: false,
    )
}

fun FriendListResponseDto.toDomain(): List<Friend> =
    items.orEmpty().mapNotNull { it?.toDomain() }
