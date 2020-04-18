package dev.shog.mojor.handle.auth.token

import dev.shog.mojor.handle.auth.obj.Permission
import java.util.*

/**
 * An authorization token.
 *
 * This gives a connection permission to do something.
 * [permissions] gives the
 */
data class Token(
        val token: String,
        val owner: UUID,
        val permissions: Collection<Permission>,
        val createdOn: Long,
        val expiresOn: Long = createdOn + TokenManager.EXPIRE_AFTER
) {
    companion object {
        /**
         * Create a new [Token] using [token], but add a new created on date.
         */
        fun fromToken(token: Token, new: Long): Token =
                Token(token.token, token.owner, token.permissions, token.createdOn, new)
    }
}