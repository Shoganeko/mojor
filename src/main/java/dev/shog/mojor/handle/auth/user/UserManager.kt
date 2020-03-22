package dev.shog.mojor.handle.auth.user

import dev.shog.mojor.handle.auth.obj.ObjectPermissions
import dev.shog.mojor.handle.auth.obj.Permissions
import dev.shog.mojor.getJsonArray
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.codec.digest.DigestUtils

/**
 * Manages users.
 */
object UserManager {
    /**
     * The default permissions for a [User].
     */
    private val DEFAULT_PERMISSIONS = arrayListOf<Permissions>()

    /**
     * If the [user] has [permissions].
     */
    fun hasPermissions(user: User, permissions: ArrayList<Permissions>): Boolean =
            user.permissions.containsAll(permissions)

    /**
     * Delete the [user].
     */
    suspend fun deleteUser(user: Long) = coroutineScope {
        if (UserHolder.hasUser(user)) {
            UserHolder.removeUser(user)

            val pre = PostgreSql.createConnection()
                    .prepareStatement("DELETE FROM users.users WHERE id = ?")

            pre.setLong(1, user)

            launch { pre.executeUpdate() }
        } else throw Exception("Tried deleting a user that doesn't exist!")
    }

    /**
     * Create a new user.
     */
    suspend fun createUser(username: String, password: String, requiresHash: Boolean = false, permissions: ArrayList<Permissions> = DEFAULT_PERMISSIONS): User = coroutineScope {
        if (UserHolder.hasUser(username))
            throw Exception("Username $username already exists!")

        val hashedPassword = if (requiresHash) DigestUtils.sha512Hex(password) else password
        val id = UserIdGenerator.getNewId()
        val user = User(username, hashedPassword, id, ObjectPermissions.fromArrayList(permissions), System.currentTimeMillis())

        UserHolder.insertUser(user.id, user)
        launch { uploadUser(user, hashedPassword) }

        return@coroutineScope user
    }

    /**
     * Login using [username] and [password].
     */
    fun loginUsing(username: String, password: String, usingCaptcha: Boolean, requiresHash: Boolean = false): User? {
        val hashedPassword = if (requiresHash) DigestUtils.sha512Hex(password) else password

        if (UserHolder.hasUser(username)) {
            val user = UserHolder.getUser(username)

            if (user?.isCorrectPassword(hashedPassword) == true)
                return user.setCaptcha(usingCaptcha)
        }

        return null
    }

    /**
     * Upload a user to the database.
     */
    private suspend fun uploadUser(user: User, password: String) = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("INSERT INTO users.users (id, name, password, permissions, createdon) VALUES (?, ?, ?, ?, ?)")

        pre.setLong(1, user.id)
        pre.setString(2, user.username)
        pre.setString(3, password)
        pre.setString(4, user.permissions.getJsonArray().toString())
        pre.setLong(5, user.createdOn)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.executeUpdate() }
    }

    /**
     * Update a user's account on the database.
     */
    suspend fun updateUser(user: User) = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("UPDATE users.users SET 'name'=?, 'password'=?, 'permissions'=? WHERE 'id'=?")

        pre.setString(1, user.username)
        pre.setString(2, user.getPassword())
        pre.setString(3, user.permissions.getJsonArray().toString())
        pre.setLong(4, user.id)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.executeUpdate() }
    }
}