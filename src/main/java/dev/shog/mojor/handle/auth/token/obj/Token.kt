package dev.shog.mojor.handle.auth.token.obj

import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
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
        val expiresOn: Long = createdOn + TokenHandler.EXPIRE_AFTER
)