package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.auth.token.result.TokenRenewResult
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.getJsonArray
import dev.shog.mojor.handle.db.PostgreSql
import org.apache.commons.codec.digest.DigestUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Manages tokens.
 */
object TokenManager {
    /**
     * Expire a token after an amount of time.
     */
    const val EXPIRE_AFTER = 1000L * 60L * 24L * 6L // 6 hours.

    /**
     * The default permissions for a [Token].
     */
    private val DEFAULT_PERMISSIONS = arrayListOf<Permissions>()

    /**
     * Tokens queued to be removed
     */
    private val REMOVAL_QUEUE = ArrayList<Token>()

    /**
     * Queue [REMOVAL_QUEUE]
     */
    suspend fun queueRemoval() {
        var sqlStr = ""

        REMOVAL_QUEUE.forEach { token ->
            sqlStr += "DELETE FROM token.tokens WHERE token='${token.token}';"
        }

        PostgreSql.createConnection()
                .prepareStatement(sqlStr)
                .executeLargeUpdate()
    }

    /**
     * If [token] is expired.
     * It is expired if the token's creation time minus the current time is greater than or equal to [EXPIRE_AFTER].
     */
    fun isTokenExpired(token: Token): Boolean {
        val expired = System.currentTimeMillis() - token.createdOn >= EXPIRE_AFTER

        if (expired)
            REMOVAL_QUEUE.add(token)

        return expired
    }

    /**
     * If the [token] has [permissions].
     */
    fun hasPermissions(token: Token, permissions: ArrayList<Permissions>): Boolean =
            token.permissions.containsAll(permissions)

    /**
     * Renew the [token].
     */
    suspend fun renewToken(token: Token, time: Long = System.currentTimeMillis()): TokenRenewResult {
        val pre = PostgreSql.createConnection()
                .prepareStatement("UPDATE token.tokens SET createdon=? WHERE token=?")

        pre.setLong(1, time)
        pre.setString(2, token.token)

        pre.executeUpdate()

        val newToken = Token.fromToken(token, time)
        return TokenRenewResult(newToken, true, newToken.expiresOn)
    }

    /**
     * Disable the [token].
     */
    suspend fun disableToken(token: Token) {
        TokenHolder.removeToken(token.token)

        val pre = PostgreSql.createConnection()
                .prepareStatement("DELETE FROM token.tokens WHERE token=?")

        pre.setString(1, token.token)

        pre.executeUpdate()
    }

    /**
     * Create a unused token identifier.
     */
    private suspend fun createTokenString(): String? {
        val postgre = PostgreSql.createConnection()
        val id = DigestUtils.sha512Hex(UUID.randomUUID().toString().toByteArray(Charsets.UTF_8))

        val pre = postgre.prepareStatement("SELECT * FROM token.tokens WHERE token = ?")

        pre.setString(1, id)

        val query = pre.executeQuery()

        return if (query.next()) null else id
    }

    /**
     * Create token
     */
    private suspend fun createTokenIdentifier(): String {
        var id: String? = null

        while (id == null)
            id = createTokenString()

        return id
    }

    /**
     * Create a unique [Token] with [user] as the owner.
     */
    suspend fun createToken(user: User): Token {
        val identifier = createTokenIdentifier()

        val token = Token(identifier, user.id, user.permissions, System.currentTimeMillis())

        TokenHolder.insertToken(token.token, token)

        val prepared = PostgreSql.createConnection()
                .prepareStatement("INSERT INTO token.tokens (token, owner, createdon, permissions) VALUES (?, ?, ?, ?)")

        prepared.setString(1, token.token)
        prepared.setLong(2, token.owner)
        prepared.setLong(3, token.createdOn)
        prepared.setString(4, token.permissions.getJsonArray().toString())

        prepared.execute()

        return token
    }
}

