package dev.shog.mojor.handle.auth.token

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.runBlocking
import java.util.*
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages tokens.
 */
object TokenHolder {
    private val TOKENS = ConcurrentHashMap<String, Token>()

    /**
     * Get all tokens from the database and insert it into the map.
     */
    private suspend fun init() {
        val rs = PostgreSql
                .createConnection()
                .prepareStatement("SELECT * FROM token.tokens")
                .executeQuery()

        while (rs.next()) {
            val mapper = ObjectMapper()
            val perms = mapper.readValue<Collection<Permission>>(rs.getString("permissions"), mapper.typeFactory.constructCollectionType(
                    Collection::class.java,
                    Permission::class.java
            ))

            val token = Token(
                    rs.getString("token"),
                    UUID.fromString(rs.getString("owner")),
                    perms,
                    rs.getLong("createdOn")
            )

            insertToken(rs.getString("token"), token)
        }
    }

    init {
        runBlocking { init() }
    }

    /**
     * Get a token from [TOKENS] by their [token].
     */
    fun getToken(token: String): Token? =
            TOKENS[token]

    /**
     * Insert into [TOKENS] a new [tokenObject].
     */
    fun insertToken(token: String, tokenObject: Token) {
        TOKENS[token] = tokenObject
    }

    /**
     * Insert [pairs] into [TOKENS].
     */
    fun insertTokens(vararg pairs: Pair<String, Token>) {
        pairs.forEach { pair ->
            insertToken(pair.first, pair.second)
        }
    }

    /**
     * Remove [token] from [TOKENS].
     */
    fun removeToken(token: String) {
        TOKENS.remove(token)
    }

    /**
     * If [TOKENS] contains a token with the [token].
     */
    fun hasToken(token: String?): Boolean =
            TOKENS.containsKey(token ?: "")
}