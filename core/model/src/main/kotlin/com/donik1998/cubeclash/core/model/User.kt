package com.donik1998.cubeclash.core.model

data class User(
    val id: String,
    val displayName: String,
    val email: String? = null,
    val country: String? = null,
    val avatarUrl: String? = null,
    val elo: Int = 1200,
    val solveCount: Int = 0,
)

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
)

sealed interface AuthState {
    data object Unknown : AuthState
    data object SignedOut : AuthState
    data class SignedIn(val user: User) : AuthState
}

data class Friend(
    val user: User,
    val status: FriendStatus,
)

enum class FriendStatus { ACCEPTED, PENDING_OUTGOING, PENDING_INCOMING }
