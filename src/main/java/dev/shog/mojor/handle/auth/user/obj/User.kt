package dev.shog.mojor.handle.auth.user.obj

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.runBlocking
import java.util.*

class User(
        username: String,
        password: String,
        permissions: Collection<Permission>,
        lastLogin: UserLastLogin?,
        val id: UUID,
        val createdOn: Long
) {
    var username = username
        set(value) {
            runBlocking {
                val pre = PostgreSql.createConnection()
                        .prepareStatement("UPDATE users.users SET 'name'=? WHERE 'id'=?")

                pre.setString(1, field)
                pre.setString(2, id.toString())

                pre.executeUpdate()
            }
            field = value
        }

    private var password = password
        set(value) {
            runBlocking {
                val pre = PostgreSql.createConnection()
                        .prepareStatement("UPDATE users.users SET 'password'=? WHERE 'id'=?")

                pre.setString(1, field)
                pre.setString(2, id.toString())

                pre.executeUpdate()
            }
            field = value
        }

    var permissions = permissions
        set(value) {
            runBlocking {
                val pre = PostgreSql.createConnection()
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
            this.password == password
}