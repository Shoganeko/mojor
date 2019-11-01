package dev.shog.mojor.auth.user

import dev.shog.mojor.auth.ObjectPermissions
import dev.shog.mojor.auth.Permissions
import kotlin.collections.ArrayList
import java.util.UUID
import org.apache.commons.codec.digest.DigestUtils
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
     * Create a unused identifier for a user.
     */
    fun createUserId(): Long {
        var id = ""
        (0..17).forEach { i ->
            id += Random.nextInt(10)
        }

        val finalId = id.toLong()

        // TODO check if token already exists

        return finalId
    }

    /**
     * Delete the [user].
     */
    fun deleteUser(user: User) {
        // TODO delete user
    }

    /**
     * Create a new user.
     */
    fun createUser(username: String, hashedPassword: String): User {
        val user = User(username, hashedPassword, createUserId(), ObjectPermissions.fromArrayList(DEFAULT_PERMISSIONS), System.currentTimeMillis())

        // TODO upload user

        return user
    }
}