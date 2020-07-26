package dev.shog.mojor.api.buta.data

/**
 * Buta's information about a guild.
 */
data class ButaGuild(
        val id: String,
        val prefix: String,
        val joinMessage: String,
        val leaveMessage: String,
        val joinRole: String,
        val swearFilterMsg: String,
        val disabledCategories: List<String>,
        val swearFilterOn: Boolean
)