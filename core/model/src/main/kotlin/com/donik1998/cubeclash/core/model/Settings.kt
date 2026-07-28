package com.donik1998.cubeclash.core.model

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val timerStyle: TimerStyle = TimerStyle.HOLD,
    val inspectionEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val soundEnabled: Boolean = false,
    val lastEventId: String = WcaEvent.DEFAULT.id,
) {
    val lastEvent: WcaEvent get() = WcaEvent.fromId(lastEventId)
}

enum class ThemeMode(val label: String) { SYSTEM("System"), LIGHT("Light"), DARK("Dark") }

/**
 * How a solve starts.
 *
 * `HOLD` is the WCA-native gesture (hold the pad until it goes green, release to start).
 * `TAP` is faster but ambiguous — a tap on the pad and a tap on a button one layer up look
 * identical to a naive gesture tree, which is exactly the bug the Flutter client still carries.
 */
enum class TimerStyle(val label: String) { HOLD("Hold"), TAP("Tap") }
