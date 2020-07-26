package dev.shog.mojor.api.buta.api

import dev.shog.mojor.api.buta.data.ButaDataHandler

/**
 * A partial guild.
 */
class PartialGuild(
        val id: String,
        val name: String,
        val icon: String?,
        val owner: Boolean,
        val permissions: Long,
        val butaIn: Boolean = ButaDataHandler.exists(id.toLong()) // TODO improve
) {
    val administrator: Boolean = permissions and 0x00000008 == 0x00000008L

    val imageUrl = if (icon == null)
        "https://shog.dev/favicon.png"
    else "https://cdn.discordapp.com/icons/$id/$icon.png?"
}