package com.donik1998.cubeclash.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CountryNamesTest {

    @Test
    fun `known code maps to display name`() {
        assertEquals("Iran", CountryNames.displayName("IR"))
        assertEquals("Uzbekistan", CountryNames.displayName("UZ"))
    }

    @Test
    fun `code is case-insensitive and trimmed`() {
        assertEquals("Germany", CountryNames.displayName(" de "))
    }

    @Test
    fun `unknown code falls back to the raw upper-cased code`() {
        // A two-letter badge is still information; never a guess and never a blank.
        assertEquals("ZZ", CountryNames.displayName("zz"))
        assertEquals("XX", CountryNames.displayName("XX"))
    }

    @Test
    fun `null or blank returns null so the UI omits the country line`() {
        assertNull(CountryNames.displayName(null))
        assertNull(CountryNames.displayName(""))
        assertNull(CountryNames.displayName("   "))
    }
}
