package dev.shog.mojor.api.users.handle

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.uuid.Generators
import com.mongodb.client.model.Filters
import dev.shog.mojor.handle.ArgumentDoesntMeet
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.api.users.obj.User
import dev.shog.mojor.handle.db.Mongo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.bson.Document
import org.mindrot.jbcrypt.BCrypt
import java.util.*
import javax.xml.ws.Dispatch

/**
 * Manages users.
 */
object UserManager {
    private val cache: MutableList<User> by lazy {
        fun parsePermissions(permissions: List<String>): Collection<Permission> =
                permissions.map { permission -> Permission.valueOf(permission) }

        Mongo.getClient()
                .getDatabase("users")
                .getCollection("users")
                .find()
                .map { doc ->
                    val id = UUID.fromString(doc.getString("id"))

                    User(
                            doc.getString("name"),
                            doc.getString("password"),
                            parsePermissions(doc["permissions"] as List<String>),
                            UserLoginManager.getMostRecentLoginAttempt(id),
                            id,
                            doc.getLong("createdon")
                    )
                }
                .toMutableList()
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
            Mongo.getClient()
                    .getDatabase("users")
                    .getCollection("users")
                    .deleteOne(Filters.eq("id", id.toString()))
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
    private suspend fun uploadUser(user: User, password: String) {
        withContext(Dispatchers.Unconfined) {
            Mongo.getClient()
                    .getDatabase("users")
                    .getCollection("users")
                    .insertOne(Document(mapOf(
                            "id" to user.id.toString(),
                            "name" to user.username,
                            "password" to password,
                            "permissions" to user.permissions.map { it.toString() },
                            "createdon" to user.createdOn
                    )))
        }
    }

    /**
     * If a user already has the [name].
     */
    fun nameExists(name: String): Boolean =
            cache.any { user -> user.username.equals(name, true) }
}