package dev.shog.mojor.api.users.token.obj

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.api.users.token.handle.TokenHandler
import dev.shog.mojor.handle.db.Mongo
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
            Mongo.getClient()
                .getDatabase("users")
                .getCollection("tokens")
                .updateOne(Filters.eq("token", token), Updates.set("permissions", value))

            field = value
        }

    /**
     * The token's creation date. This is changed when renewed.
     */
    var createdOn = createdOn
        set(value) {
            Mongo.getClient()
                .getDatabase("users")
                .getCollection("tokens")
                .updateOne(Filters.eq("token", token), Updates.set("createdon", value))

            field = value
        }

    /**
     * When a token expires.
     */
    val expiresOn
        get() = createdOn + TokenHandler.EXPIRE_AFTER
}