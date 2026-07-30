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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.donik1998.cubeclash.core.designsystem.theme.CubeClashTheme
import com.donik1998.cubeclash.feature.profile.ProfileRoute
import com.donik1998.cubeclash.feature.profile.SettingsRoute
import com.donik1998.cubeclash.feature.race.RaceRoute
import com.donik1998.cubeclash.feature.stats.StatsRoute
import com.donik1998.cubeclash.feature.timer.TimerRoute
import com.donik1998.cubeclash.navigation.ProfileDestination
import com.donik1998.cubeclash.navigation.RaceDestination
import com.donik1998.cubeclash.navigation.SettingsDestination
import com.donik1998.cubeclash.navigation.StatsDestination
import com.donik1998.cubeclash.navigation.TimerDestination
import com.donik1998.cubeclash.navigation.TopLevelDestination

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
            NavHost(navController = navController, startDestination = TimerDestination) {
                composable<TimerDestination> {
                    TimerRoute(onImmersiveChange = { immersive = it })
                }
                composable<RaceDestination> {
                    RaceRoute(onImmersiveChange = { immersive = it })
                }
                composable<StatsDestination> { StatsRoute() }
                composable<ProfileDestination> {
                    ProfileRoute(
                        // Friends has no destination yet — a harmless no-op until it lands.
                        onFriends = {},
                        onSettings = { navController.navigate(SettingsDestination) },
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
