package dev.shog.mojor.api.buta.bot

import dev.shog.lib.util.logTo
import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import kong.unirest.Unirest
import kong.unirest.json.JSONArray
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope

/**
 * To prevent constant database requests on Buta and improve consistency on Buta with the online dashboard.
 */
object ButaInteraction {
    private const val butaUrl = "http://localhost:8014"

    private val username: String
    private val password: String

    init {
        val cfg = Mojor.APP.getConfigObject<Config>()

        username = cfg.postgre.username
        password = cfg.postgre.password
    }

    /**
     * Get a [guild]'s roles.
     */
    suspend fun getRoles(guild: Long): JSONArray {
        val json = coroutineScope {
            async {
                Unirest.post("$butaUrl/roles")
                        .field("username", username)
                        .field("password", password)
                        .field("id", guild.toString())
                        .asJson()
            }
        }.await()

        return if (!json.isSuccess) {
            logTo(Mojor.APP, "FATAL: Failed to get roles from Buta!")

            JSONArray()
        } else {
            json.body.array
        }
    }

    /**
     * Update a [guild]'s cached data on Buta.,
     */
    suspend fun refreshGuild(guild: Long) {
        val json = coroutineScope {
            async {
                Unirest.post("$butaUrl/refresh")
                        .field("username", username)
                        .field("password", password)
                        .field("type", "guild")
                        .field("id", guild.toString())
                        .asJson()
            }
        }.await()

        if (!json.isSuccess)
            logTo(Mojor.APP, "FATAL: Failed to refresh guild to Buta! Reason: ${json.body}")
    }
}