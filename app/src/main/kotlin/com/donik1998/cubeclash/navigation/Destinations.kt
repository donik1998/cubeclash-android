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

@Serializable
data object AuthDestination

enum class TopLevelDestination(val label: String) {
    TIMER("Timer"),
    RACE("Race"),
    STATS("Stats"),
    YOU("You"),
}
