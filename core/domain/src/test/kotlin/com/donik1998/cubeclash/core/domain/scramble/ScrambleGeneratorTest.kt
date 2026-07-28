package com.donik1998.cubeclash.core.domain.scramble

import com.donik1998.cubeclash.core.model.Scramble
import com.donik1998.cubeclash.core.model.ScrambleNotation
import com.donik1998.cubeclash.core.model.WcaEvent
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

/**
 * The invariants, re-asserted at every size — including the two cases that are easy to miss:
 * three-character faces (`3Rw2` must resolve to `3Rw`) and the legality chain **resetting at a
 * line break**, so Multi-Blind's separate scrambles validate independently.
 */
class ScrambleGeneratorTest {

    private val generator = ScrambleGenerator(Random(20260728))

    @Test
    fun `every event produces a non-empty scramble`() {
        WcaEvent.entries.forEach { event ->
            val scramble = generator.generate(event)
            assertTrue("${event.id} produced nothing", scramble.tokenCount > 0)
        }
    }

    @Test
    fun `no NxN scramble repeats a face or stacks three moves on one axis`() {
        listOf(2, 3, 4, 5, 6, 7).forEach { size ->
            repeat(50) {
                val tokens = generator.nxn(size).lines.single()
                assertNoRedundancy(tokens, "size $size")
            }
        }
    }

    @Test
    fun `megaminx keeps its seven semantic lines`() {
        val scramble = generator.megaminx()
        assertEquals(7, scramble.lines.size)
        assertEquals(ScrambleNotation.MEGAMINX, scramble.notation)
        scramble.lines.forEach { assertEquals(11, it.size) }
    }

    @Test
    fun `line breaks survive the wire round trip byte for byte`() {
        val original = generator.megaminx()
        val roundTripped = Scramble.parse(original.toWire(), ScrambleNotation.MEGAMINX)
        assertEquals(original.lines, roundTripped.lines)
        assertEquals(original.toWire(), roundTripped.toWire())
    }

    @Test
    fun `multi-blind is N independent scrambles, each legal on its own`() {
        val scramble = generator.multiBlind(cubeCount = 5)
        assertEquals(5, scramble.lines.size)
        // The legality chain resets at a line break, so each cube validates independently.
        scramble.lines.forEach { assertNoRedundancy(it, "mbld line") }
    }

    @Test
    fun `square-1 wraps on slashes because it has no spaces`() {
        val scramble = generator.squareOne()
        val roundTripped = Scramble.parse(scramble.toWire(), ScrambleNotation.SQUARE_ONE)
        assertEquals(scramble.lines, roundTripped.lines)
        assertTrue(scramble.lines.single().all { it.endsWith("/") })
    }

    @Test
    fun `scramble density steps down as the puzzle grows`() {
        assertTrue(generator.nxn(3).density.ordinal < generator.nxn(6).density.ordinal)
    }

    private fun assertNoRedundancy(tokens: List<String>, label: String) {
        val faces = tokens.map { token -> token.dropLastWhile { it == '\'' || it == '2' } }
        faces.zipWithNext().forEach { (a, b) ->
            assertTrue("$label: repeated face $a $b", a != b)
        }
        faces.windowed(3).forEach { (a, b, c) ->
            val axes = listOf(a, b, c).map { face -> face.first { it in "UDLRFB" } }.map(::axisOf)
            assertTrue("$label: three moves on one axis: $a $b $c", axes.distinct().size > 1)
        }
    }

    private fun axisOf(face: Char): String = when (face) {
        'U', 'D' -> "UD"
        'L', 'R' -> "LR"
        else -> "FB"
    }
}
