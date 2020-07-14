package dev.shog.mojor.api.users.handle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import dev.shog.mojor.handle.ArgumentDoesntMeet
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.api.users.obj.User
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.mindrot.jbcrypt.BCrypt
import java.util.*

/**
 * Manages users.
 */
object UserManager {
    private val cache: MutableList<User> = runBlocking { refreshCache() }

    /**
     * Get all users from the database.
     */
    private suspend fun refreshCache(): MutableList<User> {
        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM users.users")
                .executeQuery()

        val users = mutableListOf<User>()

        while (rs.next()) {
            val mapper = ObjectMapper()
            val id = UUID.fromString(rs.getString("id"))

            users.add(User(
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
            ))
        }

        return users
    }

    /**
     * Get all users from the database.
     */
    fun getUsers(limit: Int = 100): List<User> =
            cache.take(limit)

    /**
     * Get a user by their [username].
     */
    @Throws(NotFound::class)
    fun getUser(username: String): User =
            cache.singleOrNull { user -> user.username.equals(username, true) }
                    ?: throw NotFound("user")

    /**
     * Get a user by their [id].
     */
    @Throws(NotFound::class)
    fun getUser(id: UUID): User =
            cache.singleOrNull { user -> user.id == id }
                    ?: throw NotFound("user")

    /**
     * Delete a user by their UUID.
     */
    @Throws(NotFound::class)
    suspend fun deleteUser(id: UUID) {
        cache.removeIf { user -> user.id == id }

        withContext(Dispatchers.Unconfined) {
            PostgreSql.getConnection("Delete user $id")
                    .prepareStatement("DELETE FROM users.users WHERE id = ?")
                    .apply { setString(1, id.toString()) }
                    .executeUpdate()
        }
    }

    /**
     * Create a user with a [username] an a [password].
     */
    @Throws(ArgumentDoesntMeet::class)
    suspend fun createUser(username: String, password: String, ip: String): User {
        when {
            !UserRequirements.usernameMeets(username) ->
                throw ArgumentDoesntMeet("username")

            !UserRequirements.passwordMeets(password) ->
                throw ArgumentDoesntMeet("password")
        }

        val hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
        val id = Generators.randomBasedGenerator().generate()

        val attempt = UserLoginManager.attemptLogin(id, ip, true)

        val user = User(username, hashedPassword, arrayListOf(), attempt, id, System.currentTimeMillis())

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
    fun nameExists(name: String): Boolean =
            cache.any { user -> user.username.equals(name, true) }
}