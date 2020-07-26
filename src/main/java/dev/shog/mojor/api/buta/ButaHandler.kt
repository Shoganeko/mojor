package dev.shog.mojor.api.buta

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.InvalidAuthorization
import dev.shog.mojor.handle.file.Config
import kong.unirest.Unirest
import org.apache.commons.codec.digest.DigestUtils
import org.json.JSONArray
import org.json.JSONObject
import java.security.SecureRandom

object ButaHandler {
    /**
     * The scopes for Discord.
     */
    private val scopes = listOf("identify", "guilds")

    /**
     * Get the client credentials.
     */
    fun getClient(): Pair<String, String> {
        val cfg = Mojor.APP.getConfigObject<Config>()

        return cfg.discordId to cfg.discordSecret
    }

    /**
     * Get a token from [code].
     */
    @Throws(InvalidAuthorization::class)
    fun getToken(code: String, redirect: String = "${Mojor.BASE}/buta/callback"): DiscordToken { // TODO
        val client = getClient()

        val json = Unirest.post("https://discord.com/api/v6/oauth2/token")
                .field("client_id", client.first)
                .field("client_secret", client.second)
                .field("grant_type", "authorization_code")
                .field("scope", scopes.joinToString(" "))
                .field("redirect_uri", redirect)
                .field("code", code)
                .asJson()

        if (json.isSuccess) {
            val content = json.body.`object`

            val token = DiscordToken(
                    content.getString("token_type"),
                    content.getInt("expires_in"),
                    content.getString("refresh_token"),
                    content.getString("scope"),
                    content.getString("access_token"),
                    genId(content.getString("access_token"))
            )

            TokenHandler.uploadToken(token)

            return token
        } else
            throw InvalidAuthorization("failed callback")
    }

    /**
     * Used in the [genId]
     */
    private val rand = SecureRandom()

    /**
     * Generate a (hopefully) more secure code utilizing the [access] token granted from Discord.
     */
    private fun genId(access: String): String {
        val bytes = ByteArray(32)

        rand.nextBytes(bytes)

        return DigestUtils.sha256Hex("${String(bytes)}$access")
    }
}