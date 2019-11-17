package dev.shog.mojor.auth

import dev.shog.mojor.auth.token.Token
import dev.shog.mojor.auth.user.User

/**
 * A user login request.
 * This should result in the full user data, and the created token from the login.
 */
data class UserLoginRequest(
        val user: User,
        val token: Token
)