package dev.shog.mojor.handle.auth.user.obj

import java.util.*

data class UserLastLogin(
        val id: UUID,
        val ip: String,
        val date: Long,
        val success: Boolean
)