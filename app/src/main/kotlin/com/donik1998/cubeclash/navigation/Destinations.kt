package com.donik1998.cubeclash.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe destinations. The four tabs are the whole top level — tournaments stay nested
 * inside Race, and Settings stays inside the You tab rather than becoming a fifth thing.
 */
@Serializable
data object TimerDestination

@Serializable
data object RaceDestination

@Serializable
data object StatsDestination

@Serializable
data object ProfileDestination

/** Pushed on top of the You tab — the old Profile screen's settings form, reachable from the
 *  Settings menu row rather than being a tab of its own. */
@Serializable
data object SettingsDestination

/** Pushed on top of the Timer tab — the full session & history list, reachable from the timer. */
@Serializable
data object SessionHistoryDestination

/**
 * Pushed on top of the Timer tab from a history row. Carries only the solve id; the screen
 * resolves the full [com.donik1998.cubeclash.core.model.Solve] from the history flow, because
 * there is no by-id endpoint.
 */
@Serializable
data class SolveDetailDestination(val solveId: String)

/**
 * Pushed on top of the Stats tab when a name is tapped on a leaderboard. Carries only the tapped
 * player's id; the screen resolves the full public profile from `GET /users/:id`.
 */
@Serializable
data class PlayerProfileDestination(val userId: String)

@Serializable
data object AuthDestination

enum class TopLevelDestination(val label: String) {
    TIMER("Timer"),
    RACE("Race"),
    STATS("Stats"),
    YOU("You"),
}
