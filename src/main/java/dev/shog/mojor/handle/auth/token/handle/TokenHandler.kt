package dev.shog.mojor.handle.auth.token.handle

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.token.obj.Token
import dev.shog.mojor.handle.auth.token.result.TokenRenewResult
import dev.shog.mojor.handle.auth.user.obj.User
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.digest.DigestUtils
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

/**
 * Manages tokens.
 */
object TokenHandler {
    const val EXPIRE_AFTER = 1000L * 60L * 24L * 24L // 24 hours.

    init {
        Timer().schedule(timerTask {
            runBlocking { clearExpired() }
        }, 0, TimeUnit.HOURS.toMillis(1))
    }

    /**
     * Get [token] from [TOKEN_CACHE].
     */
    fun getCachedToken(token: String): Token? =
            TOKEN_CACHE[token]

    /**
     * The cache of token. This initializes by getting all tokens from the database.
     */
    private val TOKEN_CACHE = object : HashMap<String, Token>() {
        init {
            runBlocking {
                val rs = PostgreSql
                        .getConnection()
                        .prepareStatement("SELECT * FROM token.tokens")
                        .executeQuery()

                launch {
                    val mapper = ObjectMapper()

                    while (rs.next()) {
                        val perms = mapper.readValue<Collection<Permission>>(
                                rs.getString("permissions"),
                                mapper.typeFactory.constructCollectionType(Collection::class.java, Permission::class.java)
                        )

                        val token = Token(
                                rs.getString("token"),
                                UUID.fromString(rs.getString("owner")),
                                perms,
                                rs.getLong("createdOn")
                        )

                        put(token.token, token)
                    }
                }
            }
        }
    }

    /**
     * Get all of the expired tokens from [TOKEN_CACHE] and remove them.
     */
    private suspend fun clearExpired() {
        TOKEN_CACHE
                .filter { token -> isTokenExpired(token.value) }
                .forEach { token -> removeToken(token.value) }
    }

    /**
     * Remove [token] from [TOKEN_CACHE] and the DB.
     */
    suspend fun removeToken(token: Token) {
        TOKEN_CACHE.remove(token.token)

        PostgreSql.getConnection()
                .prepareStatement("DELETE FROM token.tokens WHERE token=?")
                .apply { setString(1, token.token) }
                .executeUpdate()
    }

    /**
     * If [token] is expired.
     * It is expired if the token's creation time minus the current time is greater than or equal to [EXPIRE_AFTER].
     */
    fun isTokenExpired(token: Token): Boolean =
            System.currentTimeMillis() - token.createdOn >= EXPIRE_AFTER

    /**
     * If the [token] has [permissions].
     */
    fun hasPermissions(token: Token, permissions: ArrayList<Permission>): Boolean =
            token.permissions.containsAll(permissions)

    /**
     * Renew the [token].
     */
    suspend fun renewToken(token: Token, time: Long = System.currentTimeMillis()): TokenRenewResult {
        PostgreSql.getConnection()
                .prepareStatement("UPDATE token.tokens SET createdon=? WHERE token=?")
                .apply {
                    setLong(1, time)
                    setString(2, token.token)
                }
                .executeUpdate()

        val newToken = Token(token.token, token.owner, token.permissions, time)

        TOKEN_CACHE.remove(token.token)
        TOKEN_CACHE[token.token] = newToken

        return TokenRenewResult(newToken, true, newToken.expiresOn)
    }

    /**
     * The secure random used for token string generation.
     */
    private val SEC_RAND = SecureRandom()

    /**
     * Create an unused token identifier.
     */
    private suspend fun getTokenIdentifier(): String {
        val bytes = ByteArray(32)
        SEC_RAND.nextBytes(bytes)
        val token = DigestUtils.sha256Hex(String(bytes))

        val query = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM token.tokens WHERE token = ?")
                .apply { setString(1, token) }
                .executeQuery()

        return if (query.next()) getTokenIdentifier() else token
    }

    /**
     * Create a unique [Token] with [user] as the owner.
     */
    suspend fun createToken(user: User): Token {
        val identifier = getTokenIdentifier()
        val token = Token(identifier, user.id, user.permissions, System.currentTimeMillis())

        TOKEN_CACHE[token.token] = token

        val prepared = PostgreSql.getConnection()
                .prepareStatement("INSERT INTO token.tokens (token, owner, createdon, permissions) VALUES (?, ?, ?, ?)")

        prepared.setString(1, token.token)
        prepared.setString(2, token.owner.toString())
        prepared.setLong(3, token.createdOn)
        prepared.setString(4, ObjectMapper().writeValueAsString(token.permissions))

        prepared.execute()

        return token
    }
}

