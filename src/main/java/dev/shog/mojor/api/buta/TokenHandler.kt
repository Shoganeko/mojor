package dev.shog.mojor.api.buta

import com.mongodb.client.model.Filters
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.db.Mongo
import org.bson.Document

object TokenHandler {
    private val tokenCache: MutableMap<String, DiscordToken> by lazy {
        Mongo.getClient()
            .getDatabase("buta")
            .getCollection("tokens")
            .find()
            .map { doc -> doc.getString("id") to getToken(doc) }
            .toMap()
            .toMutableMap()
    }

    /**
     * Get [token] from the [tokenCache].
     */
    @Throws(NotFound::class)
    fun getToken(token: String): DiscordToken =
        tokenCache[token] ?: throw NotFound("buta_token")

    /**
     * Upload [token] to the database.
     */
    fun uploadToken(token: DiscordToken) {
        tokenCache[token.id] = token

        Mongo.getClient()
            .getDatabase("buta")
            .getCollection("tokens")
            .insertOne(
                Document(
                    mapOf(
                        "token_type" to token.tokenType,
                        "expires_in" to token.expiresIn,
                        "refresh_token" to token.refreshToken,
                        "scope" to token.scope,
                        "access_token" to token.accessToken,
                        "id" to token.id
                    )
                )
            )
    }

    private fun getToken(doc: Document): DiscordToken =
        DiscordToken(
            doc.getString("token_type"),
            doc.getInteger("expires_in"),
            doc.getString("refresh_token"),
            doc.getString("scope"),
            doc.getString("access_token"),
            doc.getString("id")
        )

    @Throws(NotFound::class)
    fun deleteToken(token: String) {
        val obj = getToken(token)

        tokenCache.remove(token)

        Mongo.getClient()
            .getDatabase("buta")
            .getCollection("token")
            .deleteOne(Filters.eq("id", obj.id))
    }
}