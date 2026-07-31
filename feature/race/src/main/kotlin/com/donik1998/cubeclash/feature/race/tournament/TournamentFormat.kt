package com.donik1998.cubeclash.feature.race.tournament

import com.donik1998.cubeclash.core.designsystem.component.ChipTone
import com.donik1998.cubeclash.core.model.TournamentStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pure formatting/derivation for the tournament screens — no Compose, no state — so the copy and the
 * status→chip mapping are unit-testable and shared by the list and the detail screen.
 */

/** The human label for a status chip, mirroring Flutter's `TournamentStatus.label`. */
fun TournamentStatus.label(): String = when (this) {
    TournamentStatus.UPCOMING -> "Upcoming"
    TournamentStatus.LIVE -> "Live"
    TournamentStatus.FINISHED -> "Finished"
    TournamentStatus.UNKNOWN -> "—"
}

/** The chip tone for a status: LIVE reads as energetic (danger dot in Flutter), the rest neutral. */
fun TournamentStatus.chipTone(): ChipTone = when (this) {
    TournamentStatus.LIVE -> ChipTone.Danger
    else -> ChipTone.Neutral
}

private val startTimeFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("EEE d MMM · HH:mm", Locale.getDefault())
        .withZone(ZoneId.systemDefault())

/**
 * The bracket start time in the device's zone (the wire timestamp is UTC). Renders "Time TBD" when
 * the start is unknown rather than fabricating an epoch.
 */
fun formatStartTime(startsAt: Instant?): String =
    startsAt?.let(startTimeFormatter::format) ?: "Time TBD"

/** "22/32 entered" — the entrant count against capacity, matching Flutter's header copy. */
fun formatEntrants(entrants: Int, capacity: Int): String = "$entrants/$capacity entered"
