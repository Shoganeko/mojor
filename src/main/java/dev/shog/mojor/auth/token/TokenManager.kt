package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.auth.token.result.TokenRenewResult
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.db.PostgreSql
import dev.shog.mojor.getJsonArray
import kotlin.collections.ArrayList
import org.apache.commons.codec.digest.DigestUtils
import reactor.core.publisher.Mono
import java.util.*


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
    fun queueRemoval(): Mono<Void> {
        var sqlStr = ""

        REMOVAL_QUEUE.forEach { token ->
            sqlStr += "DELETE FROM token.tokens WHERE token='${token.token}';"
        }

        return PostgreSql.monoConnection()
                .map { sql -> sql.prepareStatement(sqlStr) }
                .map { pre -> pre.executeLargeUpdate() }
                .then()
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
    fun hasPermissions(token: Token, permissions: ArrayList<Permissions>): Boolean {
        val tokenPermissions = token.permissions.permissions

        return tokenPermissions.containsAll(permissions)
    }

    /**
     * Renew the [token].
     */
    fun renewToken(token: Token, time: Long = System.currentTimeMillis()): Mono<TokenRenewResult> =
            PostgreSql.monoConnection()
                    .map { sql -> sql.prepareStatement("UPDATE token.tokens SET createdon=? WHERE token=?") }
                    .doOnNext { pre -> pre.setLong(1, time) }
                    .doOnNext { pre -> pre.setString(2, token.token) }
                    .map { pre -> pre.executeUpdate() }
                    .map { Token.fromToken(token, time) }
                    .map { newToken -> TokenRenewResult(newToken, true, newToken.expiresOn) }
                    .onErrorReturn(TokenRenewResult(null, false, -1L))

    /**
     * Disable the [token].
     */
    fun disableToken(token: Token): Mono<Void> {
        TokenHolder.removeToken(token.token)

        return PostgreSql.monoConnection()
                .map { sql -> sql.prepareStatement("DELETE FROM token.tokens WHERE token=?") }
                .doOnNext { pre -> pre.setString(1, token.token) }
                .map { pre -> pre.executeUpdate() }
                .then()
    }

    /**
     * Create a unused token identifier.
     */
    private fun createTokenString(): Mono<String> {
        val postgre = Mono.justOrEmpty(PostgreSql.createConnection())
        val id = DigestUtils.sha512Hex(UUID.randomUUID().toString().toByteArray(Charsets.UTF_8))

        return postgre
                .map { sql -> sql!!.prepareStatement("SELECT * FROM token.tokens WHERE token = ?") }
                .doOnNext { pre -> pre.setString(1, id) }
                .map { pre -> pre.executeQuery() }
                .filter { rs -> !rs.next() }
                .map { id }
    }

    /**
     * Create token
     */
    private fun createTokenIdentifier(): Mono<String> =
            createTokenString()
                    .switchIfEmpty(createTokenString())

    /**
     * Create a unique [Token] with [user] as the owner.
     */
    fun createToken(user: User): Mono<Token> =
            createTokenIdentifier()
                    .map { tokenIdentifier ->
                        Token(
                                tokenIdentifier,
                                user.id,
                                user.permissions,
                                System.currentTimeMillis()
                        )
                    }
                    .doOnNext { token -> TokenHolder.insertToken(token.token, token) }
                    .zipWhen { token ->
                        PostgreSql.monoConnection()
                                .map { sql -> sql.prepareStatement("INSERT INTO token.tokens (token, owner, createdon, permissions) VALUES (?, ?, ?, ?)") }
                                .doOnNext { pre -> pre.setString(1, token.token) }
                                .doOnNext { pre -> pre.setLong(2, token.owner) }
                                .doOnNext { pre -> pre.setLong(3, token.createdOn) }
                                .doOnNext { pre -> pre.setString(4, token.permissions.getJsonArray().toString()) }
                                .map { pre -> pre.execute() }
                    }
                    .map { token -> token.t1 }
}