package com.donik1998.cubeclash.core.ui

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Pure display logic for the leaderboard row. The row itself is a Composable and is exercised by
 * previews; this covers the two string helpers that decide what the avatar and rank gutter show,
 * because they are the parts most likely to break silently on odd names and deep ranks.
 */
class LeaderboardRowTest {

    @Test
    fun `rank is grouped with a thousands separator`() {
        assertEquals("1", formatRank(1))
        assertEquals("42", formatRank(42))
        assertEquals("1,204", formatRank(1_204))
        assertEquals("1,000,000", formatRank(1_000_000))
    }

    @Test
    fun `a single-word name yields one initial`() {
        assertEquals("K", initialsOf("kian_r".substringBefore('_')))
        assertEquals("E", initialsOf("emma"))
    }

    @Test
    fun `separators split a name into up to two initials`() {
        assertEquals("MD", initialsOf("m.dubois"))
        assertEquals("KR", initialsOf("kian_r"))
        assertEquals("JS", initialsOf("Jean-Sebastien"))
        assertEquals("AB", initialsOf("Alice Bob Carol"))
    }

    @Test
    fun `blank or symbol-only names fall back to a placeholder`() {
        assertEquals("?", initialsOf(""))
        assertEquals("?", initialsOf("   "))
        assertEquals("?", initialsOf("___"))
    }

    @Test
    fun `initials are always upper-cased`() {
        assertEquals("A", initialsOf("alice"))
        assertEquals("YZ", initialsOf("yush_z"))
    }
}
