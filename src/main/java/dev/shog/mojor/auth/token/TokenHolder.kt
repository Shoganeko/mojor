package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.obj.ObjectPermissions
import dev.shog.mojor.handle.db.PostgreSql
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages tokens.
 */
object TokenHolder {
    val TOKENS = ConcurrentHashMap<String, Token>()

    /**
     * Get all tokens from the database and insert it into the map.
     */
    init {
        PostgreSql
                .monoConnection()
                .map { sql -> sql.prepareStatement("SELECT * FROM token.tokens") }
                .map { pre -> pre.executeQuery() }
                .subscribe { rs ->
                    while (rs.next()) {
                        val token = Token(
                                rs.getString("token"),
                                rs.getLong("owner"),
                                ObjectPermissions.fromJsonArray(
                                        JSONArray(rs.getString("permissions"))
                                ),
                                rs.getLong("createdOn")
                        )

                        insertToken(rs.getString("token"), token)
                    }
                }
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
    fun hasToken(token: String): Boolean =
            TOKENS.containsKey(token)
}