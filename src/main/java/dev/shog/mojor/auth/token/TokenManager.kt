package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.ObjectPermissions
import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.db.PostgreSql
import kotlin.collections.ArrayList
import java.util.UUID
import org.apache.commons.codec.digest.DigestUtils
import reactor.core.publisher.Mono


/**
 * Manages tokens.
 */
object TokenManager {
    /**
     * Expire a token after an amount of time.
     */
    private const val EXPIRE_AFTER = 1000L * 60L * 6L // 6 hours.

    /**
     * The default permissions for a [Token].
     */
    private val DEFAULT_PERMISSIONS = arrayListOf<Permissions>()

    /**
     * If [token] is expired.
     * It is expired if the token's creation time minus the current time is greater than or equal to [EXPIRE_AFTER].
     */
    fun isTokenExpired(token: Token) =
            System.currentTimeMillis() - token.createdOn >= EXPIRE_AFTER

    /**
     * If the [token] has [permissions].
     */
    fun hasPermissions(token: Token, permissions: ArrayList<Permissions>): Boolean {
        val tokenPermissions = token.permissions.permissions

        return tokenPermissions.containsAll(permissions)
    }

    /**
     * Renew the [token].
     * This creates a seemingly identical token, except with a different identifier.
     * This stops the token from getting expired.
     */
    fun renewToken(token: Token): Token {
        disableToken(token)

        return TODO("The new token")
    }

    /**
     * Disable the [token].
     */
    fun disableToken(token: Token) {

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
    fun createTokenIdentifier(): Mono<String> =
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
                                ObjectPermissions.fromArrayList(DEFAULT_PERMISSIONS),
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
                                .doOnNext { pre -> pre.setString(4, token.permissions.jsonArray.toString()) }
                                .map { pre -> pre.execute() }
                    }
                    .map { token -> token.t1 }
}