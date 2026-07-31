package com.donik1998.cubeclash.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class FriendTest {

    @Test
    fun `FriendStatus fromWire is lenient and never collapses an unknown into accepted`() {
        assertEquals(FriendStatus.PENDING, FriendStatus.fromWire("pending"))
        assertEquals(FriendStatus.ACCEPTED, FriendStatus.fromWire("accepted"))
        // The house rule: an unrecognised value must NOT become a live (accepted) friendship.
        assertEquals(FriendStatus.UNKNOWN, FriendStatus.fromWire("blocked"))
        assertEquals(FriendStatus.UNKNOWN, FriendStatus.fromWire(null))
    }
}
