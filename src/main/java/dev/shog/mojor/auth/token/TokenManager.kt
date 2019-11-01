package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.AuthHandler
import dev.shog.mojor.auth.ObjectPermissions
import dev.shog.mojor.auth.Permissions
import kotlin.collections.ArrayList
import java.util.UUID
import org.apache.commons.codec.digest.DigestUtils


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
     * Create a unused token identifier
     */
    fun createTokenIdentifier(): String {
        val token = DigestUtils.sha512Hex(UUID.randomUUID().toString().toByteArray(Charsets.UTF_8))

        // TODO check if token already exists

        return token
    }

    /**
     * Disable [token].
     */
    fun disableToken(token: Token) {
        // TODO disable the token
    }

    /**
     * Create a unique [Token].
     */
    fun createToken(): Token {
        val token = Token(createTokenIdentifier(), ObjectPermissions.fromArrayList(DEFAULT_PERMISSIONS), System.currentTimeMillis())

        AuthHandler.TOKENS.put(token.token, token)
        // TODO upload token

        return token
    }
}