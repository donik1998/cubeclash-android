package com.donik1998.cubeclash.core.domain.scramble

import com.donik1998.cubeclash.core.model.PuzzleFamily
import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.WcaEvent
import javax.inject.Inject
import kotlin.random.Random

/**
 * On-device scramble generation.
 *
 * All seventeen events are covered, but every generator here is **random-move**, not
 * random-*state*. That is an honest gap, stated rather than hidden: the Flutter client
 * already solves Pyraminx and Skewb directly for genuine random-state, and porting those
 * solvers (plus a two-phase Square-1 solver) is the roadmap item. What this will never do
 * is *substitute* — faking a Megaminx scramble out of 3×3 moves would look right, which is
 * worse than admitting the gap.
 *
 * The invariants every face-turn generator upholds:
 *  1. never the same face twice in a row;
 *  2. never a third consecutive move on the same axis (`U D U` is one move disguised as three);
 *  3. the legality chain **resets at a line break**, so Multi-Blind's separate scrambles
 *     validate independently.
 */
class ScrambleGenerator(private val random: Random) {

    /**
     * Dagger gets the no-arg constructor; tests get the seeded one. A default argument would
     * generate two `@Inject` constructors, which Dagger rejects outright.
     */
    @Inject
    constructor() : this(Random.Default)


    fun generate(event: WcaEvent, cubeCount: Int = DEFAULT_MBLD_CUBES): Scramble = when {
        event == WcaEvent.MBLD -> multiBlind(cubeCount)
        event.family == PuzzleFamily.NXN -> nxn(event.size ?: 3)
        event.family == PuzzleFamily.MEGAMINX -> megaminx()
        event.family == PuzzleFamily.PYRAMINX -> pyraminx()
        event.family == PuzzleFamily.SKEWB -> skewb()
        event.family == PuzzleFamily.SQUARE_ONE -> squareOne()
        event.family == PuzzleFamily.CLOCK -> clock()
        else -> nxn(3)
    }

    // --- NxN -----------------------------------------------------------------

    fun nxn(size: Int): Scramble {
        val faces = facesFor(size)
        val length = lengthFor(size)
        val tokens = ArrayList<String>(length)
        var lastFace: String? = null
        var secondLastFace: String? = null

        while (tokens.size < length) {
            val face = faces.random(random)
            if (face == lastFace) continue
            // Rule 2: block a third move on the same axis.
            if (axisOf(face) == axisOf(lastFace) && axisOf(face) == axisOf(secondLastFace)) continue

            tokens += face + MODIFIERS.random(random)
            secondLastFace = lastFace
            lastFace = face
        }
        return Scramble(listOf(tokens), ScrambleNotation.FACE_TURN)
    }

    private fun facesFor(size: Int): List<String> = when {
        size <= 2 -> listOf("U", "R", "F")
        size == 3 -> BASE_FACES
        else -> buildList {
            addAll(BASE_FACES)
            // 4×4 and 5×5 add single wide layers; 6×6+ add numbered ones (3Rw and friends).
            addAll(BASE_FACES.map { "${it}w" })
            if (size >= 6) addAll(BASE_FACES.map { "3${it}w" })
        }
    }

    private fun lengthFor(size: Int): Int = when (size) {
        2 -> 11
        3 -> 25
        4 -> 44
        5 -> 60
        6 -> 80
        else -> 100
    }

    /** `3Rw2` must resolve to the face `3Rw`, not `3R` and not `Rw`. */
    private fun axisOf(face: String?): String? {
        val letter = face?.firstOrNull { it in "UDLRFB" } ?: return null
        return AXES.entries.first { letter in it.value }.key
    }

    // --- Megaminx ------------------------------------------------------------

    /**
     * Seven lines of ten alternating R/D moves plus a U — and **the line breaks are
     * semantic**. Cubers execute it line by line; losing your place costs the attempt.
     */
    fun megaminx(): Scramble {
        val lines = (1..7).map {
            buildList {
                repeat(5) {
                    add("R" + if (random.nextBoolean()) "++" else "--")
                    add("D" + if (random.nextBoolean()) "++" else "--")
                }
                add(if (random.nextBoolean()) "U" else "U'")
            }
        }
        return Scramble(lines, ScrambleNotation.MEGAMINX)
    }

    // --- Pyraminx / Skewb ----------------------------------------------------

    fun pyraminx(): Scramble {
        val tokens = noRepeatSequence(faces = listOf("U", "L", "R", "B"), length = 10) +
            // Tips are independent of the puzzle body, so they are appended, not interleaved.
            listOf("u", "l", "r", "b").filter { random.nextBoolean() }
                .map { it + if (random.nextBoolean()) "'" else "" }
        return Scramble(listOf(tokens), ScrambleNotation.FACE_TURN)
    }

    fun skewb(): Scramble =
        Scramble(listOf(noRepeatSequence(listOf("R", "L", "U", "B"), length = 11)), ScrambleNotation.FACE_TURN)

    private fun noRepeatSequence(faces: List<String>, length: Int): List<String> {
        val out = ArrayList<String>(length)
        var last: String? = null
        while (out.size < length) {
            val face = faces.random(random)
            if (face == last) continue
            out += face + if (random.nextBoolean()) "'" else ""
            last = face
        }
        return out
    }

    // --- Square-1 ------------------------------------------------------------

    /** Slash-separated pairs. No spaces to wrap on, so the renderer breaks after each slash. */
    fun squareOne(): Scramble {
        val tokens = (1..11).map {
            var top: Int
            var bottom: Int
            do {
                top = random.nextInt(-5, 7)
                bottom = random.nextInt(-6, 6)
            } while (top == 0 && bottom == 0)
            "($top,$bottom)/"
        }
        return Scramble(listOf(tokens), ScrambleNotation.SQUARE_ONE)
    }

    // --- Clock ---------------------------------------------------------------

    /** A fixed pattern of independent dial turns — WCA-legal with no solver needed. */
    fun clock(): Scramble {
        fun turn(dial: String) = "$dial${random.nextInt(0, 7)}${if (random.nextBoolean()) "+" else "-"}"
        val tokens = buildList {
            listOf("UR", "DR", "DL", "UL", "U", "R", "D", "L", "ALL").forEach { add(turn(it)) }
            add("y2")
            listOf("U", "R", "D", "L", "ALL").forEach { add(turn(it)) }
            addAll(PINS.filter { random.nextBoolean() })
        }
        return Scramble(listOf(tokens), ScrambleNotation.CLOCK)
    }

    // --- Multi-Blind ---------------------------------------------------------

    /** N independent 3×3 scrambles — a list, not a scramble. One per line. */
    fun multiBlind(cubeCount: Int): Scramble {
        val lines = (1..cubeCount.coerceAtLeast(2)).map { nxn(3).lines.first() }
        return Scramble(lines, ScrambleNotation.MULTI)
    }

    companion object {
        const val DEFAULT_MBLD_CUBES = 3
        private val BASE_FACES = listOf("U", "D", "L", "R", "F", "B")
        private val MODIFIERS = listOf("", "'", "2")
        private val AXES = mapOf("UD" to "UD", "LR" to "LR", "FB" to "FB")
        private val PINS = listOf("UR", "DR", "DL", "UL")
    }
}
