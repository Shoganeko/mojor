package dev.shog.mojor.api.users.token.obj

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.api.users.token.handle.TokenHandler
import dev.shog.mojor.handle.db.PostgreSql
import java.util.*

/**
 * An authorization token.
 *
 * This gives a connection permission to do something.
 * [permissions] gives the
 */
class Token(
        val token: String,
        val owner: UUID,
        permissions: Collection<Permission>,
        createdOn: Long
) {
    /**
     * Renew the token. It does this by setting the creation date to when this is invoked.
     */
    fun renew() {
        createdOn = System.currentTimeMillis()
    }

    /**
     * A token's permissions.
     */
    var permissions = permissions
        set(value) {
            PostgreSql.getConnection("Updating permissions for token")
                    .prepareStatement("UPDATE token.tokens SET permissions = ? WHERE token = ?")
                    .apply {
                        setString(1, ObjectMapper().writeValueAsString(value))
                        setString(2, token)
                    }
                    .executeUpdate()

            field = value
        }

    /**
     * The token's creation date. This is changed when renewed.
     */
    var createdOn = createdOn
        set(value) {
            PostgreSql.getConnection("Updating createdOn for token")
                    .prepareStatement("UPDATE token.tokens SET createdon = ? WHERE token = ?")
                    .apply {
                        setLong(1, value)
                        setString(2, token)
                    }
                    .executeUpdate()

            field = value
        }

    /**
     * When a token expires.
     */
    val expiresOn
        get() = createdOn + TokenHandler.EXPIRE_AFTER
}