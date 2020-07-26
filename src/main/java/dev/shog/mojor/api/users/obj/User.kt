package dev.shog.mojor.api.users.obj

import com.fasterxml.jackson.annotation.JsonIgnore
import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.db.Mongo
import org.mindrot.jbcrypt.BCrypt
import java.util.*

/**
 * A user.
 */
class User(
        username: String,
        password: String,
        permissions: Collection<Permission>,
        lastLogin: UserLoginAttempt?,
        val id: UUID,
        val createdOn: Long
) {
    /**
     * A user's username
     */
    var username = username
        set(value) {
            Mongo.getClient()
                    .getDatabase("users")
                    .getCollection("users")
                    .updateOne(Filters.eq("id", id), Updates.set("name", value))

            field = value
        }

    /**
     * A hashed password.
     */
    @JsonIgnore
    var password = password
        set(value) {
            Mongo.getClient()
                    .getDatabase("users")
                    .getCollection("users")
                    .updateOne(Filters.eq("id", id), Updates.set("password", value))

            field = value
        }

    /**
     * A user's permissions.
     */
    var permissions = permissions
        set(value) {
            Mongo.getClient()
                    .getDatabase("users")
                    .getCollection("users")
                    .updateOne(Filters.eq("id", id), Updates.set("permissions", value))

            field = value
        }

    /**
     * If the hashed [password] is correct.
     */
    fun isCorrectPassword(password: String) =
            BCrypt.checkpw(password, this.password)
}