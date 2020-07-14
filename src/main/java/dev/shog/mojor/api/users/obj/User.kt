package dev.shog.mojor.api.users.obj

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.handle.ArgumentDoesntMeet
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.api.users.handle.UserManager
import dev.shog.mojor.api.users.handle.UserRequirements
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.runBlocking
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
            runBlocking {
                if (!UserRequirements.usernameMeets(username) || UserManager.nameExists(username))
                    throw ArgumentDoesntMeet("username")

                val pre = PostgreSql.getConnection("Set $id's username")
                        .prepareStatement("UPDATE users.users SET 'name'=? WHERE 'id'=?")

                pre.setString(1, field)
                pre.setString(2, id.toString())

                pre.executeUpdate()
            }
            field = value
        }

    /**
     * A hashed password.
     */
    @JsonIgnore
    var password = password
        set(value) {
            runBlocking {
                val pre = PostgreSql.getConnection("Set $id's password")
                        .prepareStatement("UPDATE users.users SET 'password'=? WHERE 'id'=?")

                pre.setString(1, field)
                pre.setString(2, id.toString())

                pre.executeUpdate()
            }
            field = value
        }

    /**
     * A user's permissions.
     */
    var permissions = permissions
        set(value) {
            runBlocking {
                val pre = PostgreSql.getConnection("Set $id's permissions")
                        .prepareStatement("UPDATE users.users SET 'permissions'=? WHERE 'id'=?")

                pre.setString(1, ObjectMapper().writeValueAsString(field))
                pre.setString(2, id.toString())

                pre.executeUpdate()
            }

            field = value
        }

    /**
     * If the hashed [password] is correct.
     */
    fun isCorrectPassword(password: String) =
            BCrypt.checkpw(password, this.password)
}