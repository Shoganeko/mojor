package dev.shog.mojor.handle.auth.user.handle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import dev.shog.mojor.handle.ArgumentDoesntMeet
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.user.obj.User
import dev.shog.mojor.handle.auth.user.obj.UserLoginAttempt
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.*
import org.mindrot.jbcrypt.BCrypt
import java.sql.ResultSet
import java.util.*

/**
 * Manages users.
 */
object UserManager {
    /**
     * Get all users from the database.
     */
    suspend fun getUsers(limit: Int = 100): MutableList<User> {
        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM users.users LIMIT ?")
                .apply { setInt(1, limit) }
                .executeQuery()

        val users = mutableListOf<User>()

        while (rs.next())
            users.add(getUser(rs))

        return users
    }

    /**
     * Get a user by their [username].
     */
    suspend fun getUser(username: String?): User? {
        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM users.users WHERE name = ?")
                .apply { setString(1, username) }
                .executeQuery()

        if (rs.next())
            return getUser(rs)
        else throw NotFound("user")
    }

    /**
     * Get a user by their [uuid].
     */
    @Throws(NotFound::class)
    suspend fun getUser(uuid: UUID?): User? {
        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM users.users WHERE id = ?")
                .apply { setString(1, uuid.toString()) }
                .executeQuery()

        if (rs.next())
            return getUser(rs)
        else throw NotFound("user")
    }

    /**
     * Parse a user from [ResultSet]
     */
    private suspend fun getUser(rs: ResultSet): User {
        val mapper = ObjectMapper()
        val id = UUID.fromString(rs.getString("id"))

        return User(
                rs.getString("name"),
                rs.getString("password"),
                mapper.readValue(
                        rs.getString("permissions"),
                        mapper.typeFactory.constructCollectionType(
                                Collection::class.java,
                                Permission::class.java
                        )
                ),
                UserLoginManager.getMostRecentLoginAttempt(id),
                id,
                rs.getLong("createdon")
        )
    }

    /**
     * See if [user] has [permissions].
     */
    fun hasPermissions(user: User, permissions: Collection<Permission>): Boolean =
            permissions.any { perm -> !user.permissions.contains(perm) }

    /**
     * Delete a user by their UUID string.
     */
    suspend fun deleteUser(user: String) =
            deleteUser(UUID.fromString(user))

    /**
     * Delete a user by their UUID.
     */
    @Throws(NotFound::class)
    suspend fun deleteUser(user: UUID) {
        val userIn = getUser(user)

        if (userIn != null) {
            withContext(Dispatchers.Unconfined) {
                PostgreSql.getConnection()
                        .prepareStatement("DELETE FROM users.users WHERE id = ?")
                        .apply { setString(1, user.toString()) }
                        .executeUpdate()
            }
        } else throw NotFound("user")
    }

    /**
     * Create a user with a [username] an a [password].
     *
     * TODO create login attempt
     */
    @Throws(ArgumentDoesntMeet::class)
    suspend fun createUser(username: String, password: String): User {
        when {
            !UserRequirements.usernameMeets(username) ->
                throw ArgumentDoesntMeet("username")

            !UserRequirements.passwordMeets(password) ->
                throw ArgumentDoesntMeet("password")
        }

        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        val id = Generators.randomBasedGenerator().generate()
        val user = User(
                username,
                hashedPassword,
                arrayListOf(),
                UserLoginAttempt(id, "0.0.0.0", System.currentTimeMillis(), true),
                id,
                System.currentTimeMillis()
        )

        UserLoginManager.attemptLogin(id, "0.0.0.0", true)

        withContext(Dispatchers.Unconfined) {
            uploadUser(user, hashedPassword)
        }

        return user
    }

    /**
     * Login using a [username] and [password].
     */
    suspend fun loginUsing(username: String, password: String, ip: String): User? {
        val user = getUser(username)
                ?: return null

        val correct = user.isCorrectPassword(password)

        UserLoginManager.attemptLogin(user.id, ip, correct)

        return if (correct)
            user
        else
            null
    }

    /**
     * Upload [user] to the database.
     */
    private fun uploadUser(user: User, password: String) {
        PostgreSql.getConnection()
                .prepareStatement("INSERT INTO users.users (id, name, password, permissions, createdon) VALUES (?, ?, ?, ?, ?)")
                .apply {
                    setString(1, user.id.toString())
                    setString(2, user.username)
                    setString(3, password)
                    setString(4, ObjectMapper().writeValueAsString(user.permissions))
                    setLong(5, user.createdOn)
                }
                .executeUpdate()
    }

    /**
     * If a user already has the [name].
     */
    fun nameExists(name: String): Boolean {
        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT id FROM users.users WHERE name = ?")
                .apply { setString(1, name) }
                .executeQuery()

        return rs.next()
    }

    /**
     * Change a user's password.
     */
    fun changePassword(user: UUID, password: String) {
        if (!UserRequirements.passwordMeets(password))
            throw ArgumentDoesntMeet("password")

        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())

        PostgreSql.getConnection()
                .prepareStatement("UPDATE users.users SET password = ? WHERE id = ? ")
                .apply {
                    setString(1, hashedPassword)
                    setString(2, user.toString())
                }
                .executeUpdate()
    }

    /**
     * Change a user's username.
     */
    fun changeUsername(user: UUID, username: String) {
        if (!UserRequirements.usernameMeets(username) || nameExists(username))
            throw ArgumentDoesntMeet("username")

        PostgreSql.getConnection()
                .prepareStatement("UPDATE users.users SET name = ? WHERE id = ? ")
                .apply {
                    setString(1, username)
                    setString(2, user.toString())
                }
                .executeUpdate()
    }
}