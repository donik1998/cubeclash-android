package com.donik1998.cubeclash.core.model

/**
 * A scramble, kept as **lines of tokens** rather than one flat string.
 *
 * Three notations lose meaning when flattened: Megaminx executes line by line and the
 * breaks are semantic; Square-1 is slash-separated with no spaces to wrap on; Multi-Blind
 * is *N independent scrambles*, one per cube. Every renderer therefore wraps **within** a
 * line and never across one.
 *
 * The wire format is unchanged: a plain newline-separated string, byte-for-byte what
 * TNoodle emits. The one guarantee the backend owes us is that `\n` round-trips.
 */
data class Scramble(
    val lines: List<List<String>>,
    val notation: ScrambleNotation,
) {
    val tokenCount: Int get() = lines.sumOf { it.size }

    val isEmpty: Boolean get() = tokenCount == 0

    /** The wire representation — exactly what goes into `POST /solves`. */
    fun toWire(): String = lines.joinToString("\n") { line -> line.joinToString(" ") }

    /** Display density follows length; see the scramble ramp in the design system. */
    val density: ScrambleDensity
        get() = when {
            tokenCount <= 25 -> ScrambleDensity.NORMAL
            tokenCount <= 50 -> ScrambleDensity.COMPACT
            tokenCount <= 90 -> ScrambleDensity.DENSE
            // Past this a scramble stops being readable at arm's length, which is the only
            // reason the card exists — so it collapses behind an expander instead of shrinking.
            else -> ScrambleDensity.COLLAPSED
        }

    companion object {
        val EMPTY = Scramble(emptyList(), ScrambleNotation.FACE_TURN)

        fun parse(raw: String, notation: ScrambleNotation): Scramble {
            val lines = raw.split('\n')
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { line -> tokenize(line, notation) }
                .filter { it.isNotEmpty() }
            return Scramble(lines, notation)
        }

        private fun tokenize(line: String, notation: ScrambleNotation): List<String> {
            val whitespaceSplit = line.split(' ', '\t').filter { it.isNotBlank() }
            // Square-1 has no spaces to split on; its unit is the slash-terminated pair.
            if (notation == ScrambleNotation.SQUARE_ONE && whitespaceSplit.size <= 1) {
                return line.split('/').filter { it.isNotBlank() }.map { "$it/" }
            }
            return whitespaceSplit
        }
    }
}

enum class ScrambleNotation { FACE_TURN, MEGAMINX, SQUARE_ONE, CLOCK, MULTI }

enum class ScrambleDensity { NORMAL, COMPACT, DENSE, COLLAPSED }
