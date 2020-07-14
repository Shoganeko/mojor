package dev.shog.mojor.api.buta.response

import dev.shog.mojor.api.buta.api.PartialGuild
import dev.shog.mojor.api.buta.data.ButaGuild

data class GetGuildResponse(
        val discordGuild: PartialGuild,
        val butaGuild: ButaGuild
)