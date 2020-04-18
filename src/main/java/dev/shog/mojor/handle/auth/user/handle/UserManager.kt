package dev.shog.mojor.handle.auth.user.handle

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.user.obj.User
import dev.shog.mojor.handle.auth.user.obj.UserLastLogin
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.*
import org.apache.commons.codec.digest.DigestUtils
import java.util.*
import kotlin.collections.ArrayList

/**
 * Manages users.
 */
object UserManager {
    private val DEFAULT_PERMISSIONS = arrayListOf<Permission>()
    val USER_LOGIN_ATTEMPTS = object : ArrayList<UserLastLogin>() {
        init {
            runBlocking {
                val rs = PostgreSql.createConnection()
                        .prepareStatement("SELECT * FROM users.signin")
                        .executeQuery()

                launch {
                    while (rs.next()) {
                        val login = UserLastLogin(
                                UUID.fromString(rs.getString("id")),
                                rs.getString("ip"),
                                rs.getLong("date"),
                                rs.getBoolean("success")
                        )

                        add(login)
                    }
                }
            }
        }
    }

    val USER_CACHE = object : ArrayList<User>() {
        init {
            runBlocking {
                val rs = PostgreSql.createConnection()
                        .prepareStatement("SELECT * FROM users.users")
                        .executeQuery()

                launch {
                    val mapper = ObjectMapper()

                    while (rs.next()) {
                        val id = UUID.fromString(rs.getString("id"))

                        val user = User(
                                rs.getString("name"),
                                rs.getString("password"),
                                mapper.readValue(
                                        rs.getString("permissions"),
                                        mapper.typeFactory.constructCollectionType(
                                                Collection::class.java,
                                                Permission::class.java
                                        )
                                ),
                                USER_LOGIN_ATTEMPTS.singleOrNull { attempt -> attempt.success && attempt.id == id },
                                id,
                                rs.getLong("createdon")
                        )

                        add(user)
                    }
                }
            }
        }
    }

    /**
     * Get a user by their [username].
     */
    fun getUser(username: String?): User? =
            USER_CACHE.singleOrNull { user -> user.username.equals(username, true) }

    /**
     * Get a user by their [uuid].
     */
    fun getUser(uuid: UUID?): User? =
            USER_CACHE.singleOrNull { user -> user.id == uuid }

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
    suspend fun deleteUser(user: UUID) = coroutineScope {
        val userIn = getUser(user)

        if (userIn != null) {
            USER_CACHE.remove(userIn)

            val pre = PostgreSql.createConnection()
                    .prepareStatement("DELETE FROM users.users WHERE id = ?")

            pre.setString(1, user.toString())

            launch { pre.executeUpdate() }
        } else throw Exception("Tried deleting a user that doesn't exist!")
    }

    /**
     * Create a user with a [username] an a [password].
     */
    suspend fun createUser(
            username: String,
            password: String,
            requiresHash: Boolean = false,
            permissions: ArrayList<Permission> = DEFAULT_PERMISSIONS
    ): User = coroutineScope {
        if (USER_CACHE.any { user -> user.username.equals(username, true) })
            throw Exception("Username $username already exists!")

        val hashedPassword = if (requiresHash)
            DigestUtils.sha512Hex(password)
        else password

        val id = UUID.randomUUID()
        val user = User(
                username,
                hashedPassword,
                permissions,
                UserLastLogin(id, "0.0.0.0", System.currentTimeMillis(), true),
                id,
                System.currentTimeMillis()
        )

        USER_CACHE.add(user)

        launch { uploadUser(user, hashedPassword) }

        return@coroutineScope user
    }

    fun loginUsing(username: String, password: String, usingCaptcha: Boolean, requiresHash: Boolean = false): User? {
        val hashedPassword = if (requiresHash) DigestUtils.sha512Hex(password) else password
        val user = getUser(username)

        if (user != null && user.isCorrectPassword(hashedPassword))
            return user

        return null
    }

    private suspend fun uploadUser(user: User, password: String) = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("INSERT INTO users.users (id, name, password, permissions, createdon) VALUES (?, ?, ?, ?, ?)")

        pre.setString(1, user.id.toString())
        pre.setString(2, user.username)
        pre.setString(3, password)
        pre.setString(4, ObjectMapper().writeValueAsString(user.permissions))
        pre.setLong(5, user.createdOn)

        return@coroutineScope withContext(Dispatchers.Unconfined) {
            pre.executeUpdate()
        }
    }
}