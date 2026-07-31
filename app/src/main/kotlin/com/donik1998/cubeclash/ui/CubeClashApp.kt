package com.donik1998.cubeclash.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import androidx.navigation.toRoute
import com.donik1998.cubeclash.feature.auth.AuthMode
import com.donik1998.cubeclash.feature.auth.AuthRoute
import com.donik1998.cubeclash.feature.auth.setup.ProfileSetupRoute
import com.donik1998.cubeclash.feature.auth.welcome.WelcomeRoute
import com.donik1998.cubeclash.feature.profile.ProfileRoute
import com.donik1998.cubeclash.feature.profile.SettingsRoute
import com.donik1998.cubeclash.feature.profile.friends.FriendsRoute
import com.donik1998.cubeclash.feature.race.RaceRoute
import com.donik1998.cubeclash.feature.race.tournament.TournamentDetailRoute
import com.donik1998.cubeclash.feature.stats.StatsRoute
import com.donik1998.cubeclash.feature.stats.player.PlayerProfileRoute
import com.donik1998.cubeclash.feature.timer.TimerRoute
import com.donik1998.cubeclash.feature.timer.detail.SolveDetailRoute
import com.donik1998.cubeclash.feature.timer.history.SessionHistoryRoute
import com.donik1998.cubeclash.navigation.AuthDestination
import com.donik1998.cubeclash.navigation.FriendsDestination
import com.donik1998.cubeclash.navigation.PlayerProfileDestination
import com.donik1998.cubeclash.navigation.ProfileDestination
import com.donik1998.cubeclash.navigation.ProfileSetupDestination
import com.donik1998.cubeclash.navigation.RaceDestination
import com.donik1998.cubeclash.navigation.SessionHistoryDestination
import com.donik1998.cubeclash.navigation.SettingsDestination
import com.donik1998.cubeclash.navigation.SolveDetailDestination
import com.donik1998.cubeclash.navigation.StatsDestination
import com.donik1998.cubeclash.navigation.TimerDestination
import com.donik1998.cubeclash.navigation.TournamentDetailDestination
import com.donik1998.cubeclash.navigation.TopLevelDestination
import com.donik1998.cubeclash.navigation.WelcomeDestination

/**
 * The tab shell.
 *
 * Immersive flows — a running solve, a live race — hide the bar rather than living outside the
 * shell, because the thing that must not happen is a mis-tap mid-attempt. Timer is the default
 * tab: the app opens on the thing people opened it for.
 */
@Composable
fun CubeClashApp() {
    val navController = rememberNavController()
    var selected by remember { mutableStateOf(TopLevelDestination.TIMER) }
    var immersive by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CubeClashTheme.colors.canvas),
        containerColor = CubeClashTheme.colors.canvas,
        bottomBar = {
            AnimatedVisibility(
                visible = !immersive,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
            ) {
                CubeClashBottomBar(
                    selected = selected,
                    onSelect = { destination ->
                        selected = destination
                        navController.navigate(destination.route()) {
                            // Each tab keeps its own state; re-tapping never stacks a duplicate.
                            popUpTo(TimerDestination) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                )
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .padding(if (immersive) androidx.compose.foundation.layout.PaddingValues() else padding),
        ) {
            NavHost(navController = navController, startDestination = WelcomeDestination) {
                // --- Pre-shell auth flow. The bar has no place here: there is nowhere to tab to
                // yet, so each of these routes hides it through the same immersive flag a running
                // solve uses, rather than inventing a second mechanism.
                composable<WelcomeDestination> {
                    LaunchedEffect(Unit) { immersive = true }
                    WelcomeRoute(
                        onContinue = { mode -> navController.navigate(AuthDestination(mode)) },
                    )
                }
                composable<AuthDestination> { entry ->
                    LaunchedEffect(Unit) { immersive = true }
                    val destination = entry.toRoute<AuthDestination>()
                    AuthRoute(
                        initialMode = destination.initialMode,
                        onAuthenticated = { mode ->
                            when (mode) {
                                // A fresh sign-up finishes at profile setup; a returning sign-in
                                // drops straight into the shell, clearing the auth flow behind it.
                                AuthMode.SIGN_UP -> navController.navigate(ProfileSetupDestination)
                                AuthMode.SIGN_IN -> navController.enterShell()
                            }
                        },
                    )
                }
                composable<ProfileSetupDestination> {
                    LaunchedEffect(Unit) { immersive = true }
                    ProfileSetupRoute(onDone = { navController.enterShell() })
                }

                composable<TimerDestination> {
                    // Back in the shell: the bar returns unless a solve/race asks it away again.
                    LaunchedEffect(Unit) { immersive = false }
                    TimerRoute(
                        onImmersiveChange = { immersive = it },
                        onOpenHistory = { navController.navigate(SessionHistoryDestination) },
                    )
                }
                composable<SessionHistoryDestination> {
                    SessionHistoryRoute(
                        onBack = { navController.popBackStack() },
                        onOpenSolve = { solveId ->
                            navController.navigate(SolveDetailDestination(solveId))
                        },
                    )
                }
                composable<SolveDetailDestination> {
                    SolveDetailRoute(onBack = { navController.popBackStack() })
                }
                composable<RaceDestination> {
                    RaceRoute(
                        onImmersiveChange = { immersive = it },
                        onOpenTournament = { tournamentId ->
                            navController.navigate(TournamentDetailDestination(tournamentId))
                        },
                    )
                }
                composable<TournamentDetailDestination> {
                    TournamentDetailRoute(onBack = { navController.popBackStack() })
                }
                composable<StatsDestination> {
                    StatsRoute(
                        onOpenPlayer = { userId ->
                            navController.navigate(PlayerProfileDestination(userId))
                        },
                    )
                }
                composable<PlayerProfileDestination> {
                    PlayerProfileRoute(onBack = { navController.popBackStack() })
                }
                composable<ProfileDestination> {
                    ProfileRoute(
                        onFriends = { navController.navigate(FriendsDestination) },
                        onSettings = { navController.navigate(SettingsDestination) },
                    )
                }
                composable<FriendsDestination> {
                    FriendsRoute(
                        onBack = { navController.popBackStack() },
                        onOpenPlayer = { userId ->
                            navController.navigate(PlayerProfileDestination(userId))
                        },
                    )
                }
                composable<SettingsDestination> {
                    SettingsRoute(onBack = { navController.popBackStack() })
                }
            }
        }
    }
}

private fun TopLevelDestination.route(): Any = when (this) {
    TopLevelDestination.TIMER -> TimerDestination
    TopLevelDestination.RACE -> RaceDestination
    TopLevelDestination.STATS -> StatsDestination
    TopLevelDestination.YOU -> ProfileDestination
}

/**
 * Enters the tab shell on the Timer tab and drops the whole auth flow behind it, so Back from the
 * timer leaves the app rather than walking back into welcome/setup.
 */
private fun NavController.enterShell() = navigate(TimerDestination) {
    popUpTo(WelcomeDestination) { inclusive = true }
    launchSingleTop = true
}
