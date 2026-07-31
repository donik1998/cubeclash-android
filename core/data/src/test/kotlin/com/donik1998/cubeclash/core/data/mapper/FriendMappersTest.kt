package com.donik1998.cubeclash.core.data.mapper

import com.donik1998.cubeclash.core.model.FriendStatus
import com.donik1998.cubeclash.core.network.dto.FriendDto
import com.donik1998.cubeclash.core.network.dto.FriendListResponseDto
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FriendMappersTest {

    private fun dto(
        userId: String? = "f-1",
        displayName: String? = "kian_r",
        status: String? = "accepted",
        country: String? = "IR",
        avatarUrl: String? = "https://cdn/x.png",
        bestSingleMs: Long? = 6310,
        incoming: Boolean? = false,
    ) = FriendDto(userId, displayName, status, country, avatarUrl, bestSingleMs, incoming)

    @Test
    fun `a full payload maps to the domain`() {
        val friend = dto().toDomain()!!
        assertEquals("f-1", friend.userId)
        assertEquals("kian_r", friend.displayName)
        assertEquals(FriendStatus.ACCEPTED, friend.status)
        assertEquals("IR", friend.countryCode)
        assertEquals("https://cdn/x.png", friend.avatarUrl)
        assertEquals(6310L, friend.bestSingleMs)
        assertFalse(friend.incoming)
    }

    @Test
    fun `an all-null row is dropped for missing identity`() {
        assertNull(FriendDto().toDomain())
    }

    @Test
    fun `a row with a blank user_id is dropped`() {
        assertNull(dto(userId = "   ").toDomain())
        assertNull(dto(userId = null).toDomain())
    }

    @Test
    fun `a row with a null or blank display_name is dropped`() {
        assertNull(dto(displayName = null).toDomain())
        assertNull(dto(displayName = "  ").toDomain())
    }

    @Test
    fun `an unknown status maps to UNKNOWN, never accepted`() {
        assertEquals(FriendStatus.UNKNOWN, dto(status = "blocked").toDomain()!!.status)
        assertEquals(FriendStatus.UNKNOWN, dto(status = null).toDomain()!!.status)
    }

    @Test
    fun `optional fields stay null and incoming defaults to false`() {
        val friend = dto(
            country = null,
            avatarUrl = null,
            bestSingleMs = null,
            incoming = null,
        ).toDomain()!!
        assertNull(friend.countryCode)
        assertNull(friend.avatarUrl)
        assertNull(friend.bestSingleMs)
        assertFalse(friend.incoming)
    }

    @Test
    fun `an incoming request is flagged so the Accept action is offered`() {
        assertTrue(dto(status = "pending", incoming = true).toDomain()!!.incoming)
    }

    @Test
    fun `one bad element does not blank the list`() {
        val response = FriendListResponseDto(
            items = listOf(
                dto(userId = "a", displayName = "a"),
                FriendDto(), // all-null: dropped
                null, // null element: dropped
                dto(userId = "b", displayName = "b"),
            ),
            nextCursor = "c2",
        )
        val friends = response.toDomain()
        assertEquals(2, friends.size)
        assertEquals(listOf("a", "b"), friends.map { it.userId })
    }

    @Test
    fun `a null items list yields an empty list, not a crash`() {
        assertEquals(0, FriendListResponseDto().toDomain().size)
    }
}
