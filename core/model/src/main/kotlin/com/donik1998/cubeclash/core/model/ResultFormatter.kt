package com.donik1998.cubeclash.core.model

import kotlin.math.abs

/**
 * Turns a [SolveResult] into the string on screen.
 *
 * The rule that is easy to get wrong: **Regulation 9f2** — results of ten minutes or more,
 * *and all Multi-Blind results whatever their length*, truncate to whole seconds. Printing
 * `10:00.00` claims a precision the Regulations do not recognise.
 */
object ResultFormatter {

    private const val TEN_MINUTES_MS = 10L * 60L * 1000L

    fun format(result: SolveResult): String = when (result) {
        is SolveResult.Timed -> when {
            result.isDnf -> "DNF"
            else -> formatDuration(result.effectiveTimeMs) +
                if (result.penalty == Penalty.PLUS_TWO) "+" else ""
        }

        is SolveResult.MoveCount -> result.moves?.toString() ?: "DNF"

        is SolveResult.MultiBlind -> when {
            result.isDnf -> "DNF"
            // "11/13 in 54:22" — a compound result, always truncated to seconds.
            else -> "${result.solvedCount}/${result.attemptedCount} in " +
                formatDuration(result.timeMs, forceSeconds = true)
        }
    }

    /** The running clock. Same shape as a finished result so the readout does not jump. */
    fun formatRunning(elapsedMs: Long): String = formatDuration(elapsedMs)

    fun formatDuration(ms: Long, forceSeconds: Boolean = false): String {
        val truncateToSeconds = forceSeconds || ms >= TEN_MINUTES_MS
        val total = abs(ms)
        val hours = total / 3_600_000L
        val minutes = (total % 3_600_000L) / 60_000L
        val seconds = (total % 60_000L) / 1000L
        val hundredths = (total % 1000L) / 10L

        val body = when {
            hours > 0 && truncateToSeconds ->
                "%d:%02d:%02d".format(hours, minutes, seconds)

            hours > 0 ->
                "%d:%02d:%02d.%02d".format(hours, minutes, seconds, hundredths)

            minutes > 0 && truncateToSeconds ->
                "%d:%02d".format(minutes, seconds)

            minutes > 0 ->
                "%d:%02d.%02d".format(minutes, seconds, hundredths)

            truncateToSeconds -> "%d".format(seconds)

            else -> "%d.%02d".format(seconds, hundredths)
        }
        return if (ms < 0) "-$body" else body
    }

    /** An average is a duration too, but a DNF average has to say so. */
    fun formatAverage(value: Long?, event: WcaEvent): String = when {
        value == null -> "—"
        value == SolveResult.DNF_RANK -> "DNF"
        event.resultKind == ResultKind.MOVE_COUNT -> "%.2f".format(value / 100.0)
        else -> formatDuration(value)
    }
}
