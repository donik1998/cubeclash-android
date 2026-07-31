package com.donik1998.cubeclash.navigation

import com.donik1998.cubeclash.feature.auth.AuthMode
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

/**
 * Pushed on top of the You tab from the Profile screen's Friends row. The viewer's friends list,
 * the incoming/outgoing invite sections and the invite-by-handle flow. Tapping a friend pushes
 * [PlayerProfileDestination] — the same public-profile screen a leaderboard name opens.
 */
@Serializable
data object FriendsDestination

/**
 * Pushed on top of the Race tab from a row in the Tournaments lobby tab. Carries only the tapped
 * tournament's id; the screen resolves the full [com.donik1998.cubeclash.core.model.TournamentDetail]
 * (header plus bracket) from `GET /tournaments/{id}`.
 */
@Serializable
data class TournamentDetailDestination(val tournamentId: String)

/**
 * The landing screen, and the app's start destination while signed out. Pre-shell, so the tab bar
 * stays hidden. Its two actions push [AuthDestination] with the mode already chosen.
 */
@Serializable
data object WelcomeDestination

/**
 * The email/password form. [initialMode] is which side of the segmented control it opens on —
 * `SIGN_UP` from "Create an account", `SIGN_IN` from "I already have one".
 */
@Serializable
data class AuthDestination(val initialMode: AuthMode = AuthMode.SIGN_IN)

/**
 * The last step of registration — display name and optional country. Pre-shell, so the tab bar
 * stays hidden until it advances into the shell.
 */
@Serializable
data object ProfileSetupDestination

enum class TopLevelDestination(val label: String) {
    TIMER("Timer"),
    RACE("Race"),
    STATS("Stats"),
    YOU("You"),
}
