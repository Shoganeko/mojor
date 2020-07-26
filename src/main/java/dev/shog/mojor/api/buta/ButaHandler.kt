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
    private val scopes = listOf("identify", "guilds")

    fun getClient(): Pair<String, String> {
        val cfg = Mojor.APP.getConfigObject<Config>()

        return cfg.discordId to cfg.discordSecret
    }

    @Throws(InvalidAuthorization::class)
    fun getToken(code: String, redirect: String = "http://localhost:8080/buta/callback"): DiscordToken { // TODO
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

    @Throws(InvalidAuthorization::class)
    fun refreshToken(token: DiscordToken, redirect: String = "http://localhost:8080/buta/callback") {
        val client = getClient()

        val json = Unirest.post("https://discord.com/api/v6/oauth2/token")
                .field("client_id", client.first)
                .field("client_secret", client.second)
                .field("grant_type", "refresh_token")
                .field("scope", scopes.joinToString(" "))
                .field("redirect_uri", redirect)
                .field("refresh_token", token.refreshToken)
                .asJson()
    }

    private val rand = SecureRandom()

    private fun genId(access: String): String {
        val bytes = ByteArray(32)

        rand.nextBytes(bytes)

        return DigestUtils.sha256Hex("${String(bytes)}$access")
    }
}