package com.donik1998.cubeclash.core.model

/**
 * The seventeen official WCA events.
 *
 * This registry is the client-side twin of the server's `src/domain/wca-events.ts`.
 * The two are separate code on purpose — the server only carries what it must
 * *enforce* (wire id, result kind, format, raceability), while notation, icon
 * composition, grouping and search aliases are display concerns that belong here.
 * What keeps them honest is that both are tested against the same facts: the WCA
 * Regulations, quoted by number in [EventFormat] and asserted in `WcaEventTest`.
 *
 * Ids extend the ones already in the database (`3x3`, `4x4`) rather than the WCA's
 * own `333`/`444` codes: existing rows stay valid and the ids stay readable in a log.
 */
enum class WcaEvent(
    /** The wire id. Also the `event` column value and the `?event=` query parameter. */
    val id: String,
    val displayName: String,
    /** The base puzzle. `3BLD` *is* a 3×3 — cubers name it that way, so the icon does too. */
    val family: PuzzleFamily,
    /** Cube order for [PuzzleFamily.NXN], `null` for everything else. */
    val size: Int?,
    val modifier: PuzzleModifier,
    val format: EventFormat,
    val notation: ScrambleNotation,
    val resultKind: ResultKind,
    val group: EventGroup,
    /** Whether a private race room will accept this event. */
    val raceable: Boolean,
    /** Whether the matchmaking queue will search for it. Strictly narrower than [raceable]. */
    val quickMatch: Boolean,
    /**
     * Kept as a gate rather than hard-coded `true`: [fromId] is lenient, so a future
     * event that ships before its scrambler should degrade gracefully rather than crash.
     */
    val hasScrambler: Boolean,
    /** The names cubers actually type. The picker searches these, not [displayName]. */
    val aliases: List<String>,
    /** Attempt cap inherent to the event (Regulations E2, H1b), in milliseconds. */
    val attemptLimitMs: Long? = null,
) {
    // --- Cubes ---------------------------------------------------------------
    TWO(
        id = "2x2", displayName = "2×2 Cube", family = PuzzleFamily.NXN, size = 2,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.CUBES, raceable = true, quickMatch = true, hasScrambler = true,
        aliases = listOf("2x2", "222", "two"),
    ),
    THREE(
        id = "3x3", displayName = "3×3 Cube", family = PuzzleFamily.NXN, size = 3,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.CUBES, raceable = true, quickMatch = true, hasScrambler = true,
        aliases = listOf("3x3", "333", "three"),
    ),
    FOUR(
        id = "4x4", displayName = "4×4 Cube", family = PuzzleFamily.NXN, size = 4,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.CUBES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("4x4", "444", "four"),
    ),
    FIVE(
        id = "5x5", displayName = "5×5 Cube", family = PuzzleFamily.NXN, size = 5,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.CUBES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("5x5", "555", "five"),
    ),
    SIX(
        id = "6x6", displayName = "6×6 Cube", family = PuzzleFamily.NXN, size = 6,
        modifier = PuzzleModifier.NONE, format = EventFormat.MO3,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.CUBES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("6x6", "666", "six"),
    ),
    SEVEN(
        id = "7x7", displayName = "7×7 Cube", family = PuzzleFamily.NXN, size = 7,
        modifier = PuzzleModifier.NONE, format = EventFormat.MO3,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.CUBES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("7x7", "777", "seven"),
    ),

    // --- Other puzzles -------------------------------------------------------
    THREE_OH(
        id = "3x3-oh", displayName = "3×3 One-Handed", family = PuzzleFamily.NXN, size = 3,
        modifier = PuzzleModifier.ONE_HANDED, format = EventFormat.AO5,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.OTHER_PUZZLES, raceable = true, quickMatch = true, hasScrambler = true,
        aliases = listOf("oh", "one handed", "3oh"),
    ),
    CLOCK(
        id = "clock", displayName = "Clock", family = PuzzleFamily.CLOCK, size = null,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.CLOCK, resultKind = ResultKind.TIME,
        group = EventGroup.OTHER_PUZZLES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("clock"),
    ),
    MEGAMINX(
        id = "megaminx", displayName = "Megaminx", family = PuzzleFamily.MEGAMINX, size = null,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.MEGAMINX, resultKind = ResultKind.TIME,
        group = EventGroup.OTHER_PUZZLES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("mega", "minx"),
    ),
    PYRAMINX(
        id = "pyraminx", displayName = "Pyraminx", family = PuzzleFamily.PYRAMINX, size = null,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.OTHER_PUZZLES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("pyra"),
    ),
    SKEWB(
        id = "skewb", displayName = "Skewb", family = PuzzleFamily.SKEWB, size = null,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.OTHER_PUZZLES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("skewb"),
    ),
    SQUARE_ONE(
        id = "square-1", displayName = "Square-1", family = PuzzleFamily.SQUARE_ONE, size = null,
        modifier = PuzzleModifier.NONE, format = EventFormat.AO5,
        notation = ScrambleNotation.SQUARE_ONE, resultKind = ResultKind.TIME,
        group = EventGroup.OTHER_PUZZLES, raceable = true, quickMatch = false, hasScrambler = true,
        aliases = listOf("sq1", "square1", "sq-1"),
    ),

    // --- Blindfolded ---------------------------------------------------------
    // Never raceable: the memorisation phase sits *inside* the timed attempt, and
    // nothing on screen can tell concentrating from stalling.
    THREE_BLD(
        id = "3x3-bld", displayName = "3×3 Blindfolded", family = PuzzleFamily.NXN, size = 3,
        modifier = PuzzleModifier.BLINDFOLDED, format = EventFormat.BO3,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.BLINDFOLDED, raceable = false, quickMatch = false, hasScrambler = true,
        aliases = listOf("3bld", "bld", "blind"),
    ),
    FOUR_BLD(
        id = "4x4-bld", displayName = "4×4 Blindfolded", family = PuzzleFamily.NXN, size = 4,
        modifier = PuzzleModifier.BLINDFOLDED, format = EventFormat.BO3,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.BLINDFOLDED, raceable = false, quickMatch = false, hasScrambler = true,
        aliases = listOf("4bld"),
    ),
    FIVE_BLD(
        id = "5x5-bld", displayName = "5×5 Blindfolded", family = PuzzleFamily.NXN, size = 5,
        modifier = PuzzleModifier.BLINDFOLDED, format = EventFormat.BO3,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.TIME,
        group = EventGroup.BLINDFOLDED, raceable = false, quickMatch = false, hasScrambler = true,
        aliases = listOf("5bld"),
    ),

    // --- Special -------------------------------------------------------------
    FMC(
        id = "3x3-fmc", displayName = "3×3 Fewest Moves", family = PuzzleFamily.NXN, size = 3,
        modifier = PuzzleModifier.FEWEST_MOVES, format = EventFormat.MO3,
        notation = ScrambleNotation.FACE_TURN, resultKind = ResultKind.MOVE_COUNT,
        group = EventGroup.SPECIAL, raceable = false, quickMatch = false, hasScrambler = true,
        aliases = listOf("fmc", "fewest"), attemptLimitMs = HOUR_MS,
    ),
    MBLD(
        id = "3x3-mbld", displayName = "3×3 Multi-Blind", family = PuzzleFamily.NXN, size = 3,
        modifier = PuzzleModifier.MULTI_BLIND, format = EventFormat.BO1,
        notation = ScrambleNotation.MULTI, resultKind = ResultKind.MULTI_BLIND,
        group = EventGroup.SPECIAL, raceable = false, quickMatch = false, hasScrambler = true,
        aliases = listOf("mbld", "multi"), attemptLimitMs = HOUR_MS,
    ),
    ;

    /**
     * A `+2` is a *time* penalty. Two seconds added to a move count is nonsense, and
     * Multi-Blind has no inspection to overrun — so the chip is hidden for both rather
     * than offered as a button that would lie. A DNF, by contrast, applies to everything:
     * any attempt can fail.
     */
    val supportsPlusTwo: Boolean get() = resultKind == ResultKind.TIME

    /** FMC and Multi-Blind results are entered by hand; neither is ranked on the clock. */
    val entersResultManually: Boolean get() = resultKind != ResultKind.TIME

    fun matches(query: String): Boolean {
        val q = query.trim().lowercase()
        if (q.isEmpty()) return true
        return id.contains(q) ||
            displayName.lowercase().contains(q) ||
            aliases.any { it.contains(q) }
    }

    companion object {
        val DEFAULT: WcaEvent = THREE

        val raceable: List<WcaEvent> get() = entries.filter { it.raceable }
        val quickMatch: List<WcaEvent> get() = entries.filter { it.quickMatch }

        /**
         * Deliberately lenient: an unknown id falls back to 3×3 rather than throwing, so a
         * server that grows an eighteenth event before the client does cannot crash the timer.
         */
        fun fromId(id: String?): WcaEvent = entries.firstOrNull { it.id == id } ?: DEFAULT

        fun grouped(): Map<EventGroup, List<WcaEvent>> = entries.groupBy { it.group }
    }
}

private const val HOUR_MS = 60L * 60L * 1000L

/** The base shape an icon is drawn from. Eleven puzzles cover all seventeen events. */
enum class PuzzleFamily { NXN, MEGAMINX, PYRAMINX, SKEWB, SQUARE_ONE, CLOCK }

/** The badge composed onto the base shape. Five disciplines cover the rest. */
enum class PuzzleModifier { NONE, BLINDFOLDED, ONE_HANDED, FEWEST_MOVES, MULTI_BLIND }

/** How an attempt's result is expressed — decides ranking and which columns a solve may carry. */
enum class ResultKind { TIME, MOVE_COUNT, MULTI_BLIND }

enum class EventGroup(val label: String) {
    CUBES("Cubes"),
    OTHER_PUZZLES("Other puzzles"),
    BLINDFOLDED("Blindfolded"),
    SPECIAL("Special"),
}
