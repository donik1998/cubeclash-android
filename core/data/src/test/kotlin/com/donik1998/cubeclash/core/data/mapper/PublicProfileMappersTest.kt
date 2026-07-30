package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.network.dto.HeadToHeadDto
import com.donik1998.cubeclash.core.network.dto.PublicUserDto
import com.donik1998.cubeclash.core.network.dto.PublicUserResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class PublicProfileMappersTest {

    private fun user(
        id: String = "u-1",
        displayName: String? = "Probe",
        country: String? = "UZ",
        elo: Int? = 1180,
        bestSingleMs: Long? = 7137,
        ao5: Long? = 7411,
        ao12: Long? = 7800,
        headToHead: HeadToHeadDto? = HeadToHeadDto(wins = 3, losses = 1),
    ) = PublicUserDto(id, displayName, country, elo, bestSingleMs, ao5, ao12, headToHead)

    @Test
    fun `a full payload maps to the domain`() {
        val profile = PublicUserResponseDto(user()).toDomain()

        assertNotNull(profile)
        assertEquals("u-1", profile!!.id)
        assertEquals("Probe", profile.displayName)
        assertEquals("UZ", profile.country)
        assertEquals(1180, profile.elo)
        assertEquals(7137L, profile.bestSingleMs)
        assertEquals(7411L, profile.bestAo5Ms)
        assertEquals(7800L, profile.bestAo12Ms)
        assertEquals(3, profile.headToHead?.wins)
        assertEquals(1, profile.headToHead?.losses)
    }

    @Test
    fun `an all-nulls user keeps identity but leaves optionals null and elo defaulted`() {
        // The verified fresh-account shape: no solves, no country, never raced.
        val profile = PublicUserResponseDto(
            user(
                country = null,
                elo = null,
                bestSingleMs = null,
                ao5 = null,
                ao12 = null,
                headToHead = null,
            ),
        ).toDomain()

        assertNotNull(profile)
        assertEquals("u-1", profile!!.id)
        assertEquals("Probe", profile.displayName)
        assertNull(profile.country)
        assertEquals(1000, profile.elo) // server default, not fabricated
        assertNull(profile.bestSingleMs)
        assertNull(profile.bestAo5Ms)
        assertNull(profile.bestAo12Ms)
        assertNull(profile.headToHead)
    }

    @Test
    fun `head_to_head null and zero-zero produce distinguishable results`() {
        val neverRaced = PublicUserResponseDto(user(headToHead = null)).toDomain()
        val racedNoWins = PublicUserResponseDto(
            user(headToHead = HeadToHeadDto(wins = 0, losses = 0)),
        ).toDomain()

        // null on the wire -> null in the model: "never raced", a distinct UI state.
        assertNull(neverRaced?.headToHead)
        // a present 0-0 object survives as 0-0, NOT collapsed to null.
        assertNotNull(racedNoWins?.headToHead)
        assertEquals(0, racedNoWins!!.headToHead!!.wins)
        assertEquals(0, racedNoWins.headToHead!!.losses)
    }

    @Test
    fun `a present head_to_head with a missing count defaults that side to zero`() {
        val profile = PublicUserResponseDto(
            user(headToHead = HeadToHeadDto(wins = 4, losses = null)),
        ).toDomain()

        assertEquals(4, profile?.headToHead?.wins)
        assertEquals(0, profile?.headToHead?.losses)
    }

    @Test
    fun `a payload with no user object is dropped`() {
        assertNull(PublicUserResponseDto(user = null).toDomain())
    }

    @Test
    fun `a user with a blank id is dropped`() {
        assertNull(PublicUserResponseDto(user(id = "   ")).toDomain())
    }

    @Test
    fun `a user with a null or blank display_name is dropped`() {
        assertNull(PublicUserResponseDto(user(displayName = null)).toDomain())
        assertNull(PublicUserResponseDto(user(displayName = "  ")).toDomain())
    }
}
