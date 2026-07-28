package com.donik1998.cubeclash.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The event table is tested against the **Regulations**, not against itself — each assertion
 * names the fact it encodes, so an edit that contradicts the WCA fails with a pointer to the
 * authority rather than a diff.
 *
 * It is also the contract test against `cubeclash-backend/src/domain/wca-events.ts`: the two
 * registries are separate code, and this is what keeps them honest.
 */
class WcaEventTest {

    @Test
    fun `all seventeen official events ship`() {
        assertEquals(17, WcaEvent.entries.size)
    }

    @Test
    fun `wire ids match the backend registry exactly`() {
        // Mirrors WCA_EVENTS in cubeclash-backend/src/domain/wca-events.ts, in order.
        val expected = listOf(
            "2x2", "3x3", "4x4", "5x5", "6x6", "7x7",
            "3x3-oh", "clock", "megaminx", "pyraminx", "skewb", "square-1",
            "3x3-bld", "4x4-bld", "5x5-bld",
            "3x3-fmc", "3x3-mbld",
        )
        assertEquals(expected, WcaEvent.entries.map { it.id })
    }

    @Test
    fun `unknown ids fall back to 3x3 rather than throwing`() {
        // A server that grows an eighteenth event before the client does must not crash the timer.
        assertEquals(WcaEvent.THREE, WcaEvent.fromId("9x9"))
        assertEquals(WcaEvent.THREE, WcaEvent.fromId(null))
    }

    @Test
    fun `big cubes and FMC rank on mean of three - 9b2a, 9b4a`() {
        assertEquals(EventFormat.MO3, WcaEvent.SIX.format)
        assertEquals(EventFormat.MO3, WcaEvent.SEVEN.format)
        assertEquals(EventFormat.MO3, WcaEvent.FMC.format)
    }

    @Test
    fun `blindfolded events are best of three - 9b3a`() {
        listOf(WcaEvent.THREE_BLD, WcaEvent.FOUR_BLD, WcaEvent.FIVE_BLD).forEach {
            assertEquals(EventFormat.BO3, it.format)
        }
    }

    @Test
    fun `multi-blind is best of one - 9b5a`() {
        assertEquals(EventFormat.BO1, WcaEvent.MBLD.format)
        assertEquals(
            listOf(SessionStatKind.BEST, SessionStatKind.LAST, SessionStatKind.COUNT),
            WcaEvent.MBLD.format.sessionStats,
        )
    }

    @Test
    fun `twelve events are raceable and only three are quick match`() {
        assertEquals(12, WcaEvent.raceable.size)
        assertEquals(listOf("2x2", "3x3", "3x3-oh"), WcaEvent.quickMatch.map { it.id })
    }

    @Test
    fun `blindfolded FMC and multi-blind are never raceable`() {
        listOf(
            WcaEvent.THREE_BLD, WcaEvent.FOUR_BLD, WcaEvent.FIVE_BLD,
            WcaEvent.FMC, WcaEvent.MBLD,
        ).forEach { assertFalse("${it.id} must not be raceable", it.raceable) }
    }

    @Test
    fun `plus two is hidden where it would be nonsense`() {
        assertFalse(WcaEvent.FMC.supportsPlusTwo)
        assertFalse(WcaEvent.MBLD.supportsPlusTwo)
        assertTrue(WcaEvent.THREE.supportsPlusTwo)
    }

    @Test
    fun `picker searches the short names cubers type`() {
        assertTrue(WcaEvent.THREE_OH.matches("oh"))
        assertTrue(WcaEvent.THREE_BLD.matches("3bld"))
        assertTrue(WcaEvent.MEGAMINX.matches("mega"))
        assertTrue(WcaEvent.SQUARE_ONE.matches("sq1"))
    }

    @Test
    fun `long-form events carry the one-hour attempt limit - E2, H1b`() {
        assertEquals(3_600_000L, WcaEvent.FMC.attemptLimitMs)
        assertEquals(3_600_000L, WcaEvent.MBLD.attemptLimitMs)
        assertEquals(null, WcaEvent.THREE.attemptLimitMs)
    }
}
