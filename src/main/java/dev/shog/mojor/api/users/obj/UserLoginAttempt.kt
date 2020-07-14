package dev.shog.mojor.api.users.obj

import java.util.*

/**
 * A login attempt.
 */
data class UserLoginAttempt(
        val id: UUID,
        val ip: String,
        val date: Long,
        val success: Boolean
)