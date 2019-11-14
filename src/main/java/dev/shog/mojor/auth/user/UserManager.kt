package dev.shog.mojor.auth.user

import dev.shog.mojor.auth.ObjectPermissions
import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.db.PostgreSql
import reactor.core.publisher.Mono
import kotlin.random.Random

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
        return UserIdGenerator.getId()
                .map { User(username, hashedPassword, it, ObjectPermissions.fromArrayList(DEFAULT_PERMISSIONS), System.currentTimeMillis()) }
                .doOnNext { user -> UserHolder.insertUser(user.id, user) }
                .flatMap { user -> uploadUser(user).map { user } }
    }

    /**
     * Upload a user to the database.
     */
    private fun uploadUser(user: User): Mono<Void> =
            PostgreSql.monoConnection()
                    .map { sql -> sql.prepareStatement("INSERT INTO users.users (id, name, password, permissions, createdon) VALUES (?, ?, ?, ?, ?)") }
                    .doOnNext { pre -> pre.setLong(1, user.id) }
                    .doOnNext { pre -> pre.setString(2, user.username) }
                    .doOnNext { pre -> pre.setString(3, user.hashedPassword) }
                    .doOnNext { pre -> pre.setString(4, user.permissions.jsonArray.toString()) }
                    .doOnNext { pre -> pre.setLong(5, user.createdOn) }
                    .map { pre -> pre.executeUpdate() }
                    .then()
}