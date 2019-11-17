package dev.shog.mojor.auth.user

import dev.shog.mojor.auth.ObjectPermissions
import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.db.PostgreSql
import dev.shog.mojor.getJsonArray
import org.apache.commons.codec.digest.DigestUtils
import reactor.core.publisher.Mono

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
    fun hasPermissions(user: User, permissions: ArrayList<Permissions>): Boolean {
        val tokenPermissions = user.permissions.permissions

        return tokenPermissions.containsAll(permissions)
    }

    /**
     * Delete the [user].
     */
    fun deleteUser(user: Long): Mono<Void> {
        return if (UserHolder.hasUser(user)) {
            UserHolder.removeUser(user)

            PostgreSql.monoConnection()
                    .map { sql -> sql.prepareStatement("DELETE FROM users.users WHERE id = ?") }
                    .doOnNext { pre -> pre.setLong(1, user) }
                    .map { pre -> pre.executeUpdate() }
                    .then()
        } else Mono.error(Exception("Tried deleting a user that doesn't exist!"))
    }

    /**
     * Create a new user.
     */
    fun createUser(username: String, hashedPassword: String): Mono<User> {
        if (UserHolder.hasUser(username))
            return Mono.error(Exception("Username $username already exists!"))

        return UserIdGenerator.getId()
                .map { User(username, hashedPassword, it, ObjectPermissions.fromArrayList(DEFAULT_PERMISSIONS), System.currentTimeMillis()) }
                .doOnNext { user -> UserHolder.insertUser(user.id, user) }
                .flatMap { user -> uploadUser(user, hashedPassword).map { user } }
    }

    /**
     * Login using [username] and [hashedPassword]
     */
    fun loginUsing(username: String, hashedPassword: String): User? {
        if (UserHolder.hasUser(username)) {
            val user = UserHolder.getUser(username)

            if (user?.isCorrectPassword(hashedPassword) == true)
                return user
        }

        return null
    }

    /**
     * Login using [username] and [password], should be using [loginUsing]
     */
    fun loginUsingNonHashedPassword(username: String, password: String): User? {
        val hex = DigestUtils.sha512Hex(password)

        return loginUsing(username, hex)
    }

    /**
     * Upload a user to the database.
     */
    private fun uploadUser(user: User, password: String): Mono<Void> =
            PostgreSql.monoConnection()
                    .map { sql -> sql.prepareStatement("INSERT INTO users.users (id, name, password, permissions, createdon) VALUES (?, ?, ?, ?, ?)") }
                    .doOnNext { pre -> pre.setLong(1, user.id) }
                    .doOnNext { pre -> pre.setString(2, user.username) }
                    .doOnNext { pre -> pre.setString(3, password) }
                    .doOnNext { pre -> pre.setString(4, user.permissions.getJsonArray().toString()) }
                    .doOnNext { pre -> pre.setLong(5, user.createdOn) }
                    .map { pre -> pre.executeUpdate() }
                    .then()
}