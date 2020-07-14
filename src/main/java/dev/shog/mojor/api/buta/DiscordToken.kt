package dev.shog.mojor.api.buta

data class DiscordToken(
        val tokenType: String,
        val expiresIn: Int,
        val refreshToken: String,
        val scope: String,
        val accessToken: String,
        val id: String
)