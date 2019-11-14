package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.ObjectPermissions
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.auth.user.UserHolder.insertUser
import dev.shog.mojor.db.PostgreSql
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages tokens.
 */
object TokenHolder {
    private val TOKENS = ConcurrentHashMap<String, Token>()

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
                        TOKENS[rs.getString("token")] = Token(
                                rs.getString("token"),
                                rs.getLong("owner"),
                                ObjectPermissions.fromJsonArray(
                                        JSONArray(rs.getString("permissions"))
                                ),
                                rs.getLong("createdOn")
                        )
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
        assert(tokenObject.token == token)
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