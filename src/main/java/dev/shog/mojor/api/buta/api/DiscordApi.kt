package dev.shog.mojor.api.buta.api

import dev.shog.mojor.api.buta.DiscordToken
import dev.shog.mojor.handle.InvalidAuthorization
import kong.unirest.Unirest
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

object DiscordApi {
    private const val base = "https://discord.com/api/v6"

    /**
     * Get [token]'s guilds.
     */
    @Throws(InvalidAuthorization::class)
    suspend fun getGuilds(token: DiscordToken): List<PartialGuild> = coroutineScope {
        val json =
            Unirest.get("$base/users/@me/guilds")
                .header("Authorization", "Bearer ${token.accessToken}")
                .asJson()

        if (json.isSuccess) {
            val obj = json.body.array

            return@coroutineScope (0 until obj.length())
                .asSequence()
                .map { obj.getJSONObject(it) }
                .map {
                    PartialGuild(
                        it.getString("id"),
                        it.getString("name"),
                        try {
                            it.getString("icon")
                        } catch (ex: Exception) {
                            null
                        },
                        it.getBoolean("owner"),
                        it.getLong("permissions")
                    )
                }
                .toList()
        } else {
            delay(json.body.`object`.getLong("retry_after"))

            return@coroutineScope getGuilds(token)
        }
    }

    @Throws(InvalidAuthorization::class)
    fun getIdentity(token: DiscordToken): User {
        val json = Unirest.get("$base/users/@me")
            .header("Authorization", "Bearer ${token.accessToken}")
            .asJson()

        if (json.isSuccess) {
            val obj = json.body.`object`

            return User(
                obj.getLong("id"),
                obj.getString("username"),
                obj.getString("discriminator"),
                try {
                    obj.getString("avatar")
                } catch (ex: Exception) {
                    null
                }
            )
        } else
            throw InvalidAuthorization("invalid access token")
    }
}