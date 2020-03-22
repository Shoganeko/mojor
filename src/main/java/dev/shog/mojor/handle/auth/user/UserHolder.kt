package dev.shog.mojor.handle.auth.user

import dev.shog.mojor.handle.auth.obj.ObjectPermissions
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds users.
 */
object UserHolder {
    val USERS = ConcurrentHashMap<Long, User>()

    /**
     * Refresh users.
     */
    private suspend fun refreshUsers() = coroutineScope {
        val rs = withContext(Dispatchers.Default) {
            PostgreSql.createConnection()
                    .prepareStatement("SELECT * FROM users.users")
                    .executeQuery()
        }

        while (rs.next()) {
            USERS[rs.getLong("id")] = User(
                    rs.getString("name"),
                    rs.getString("password"),
                    rs.getLong("id"),
                    ObjectPermissions.fromJsonArray(JSONArray(rs.getString("permissions"))),
                    rs.getLong("createdon")
            )
        }
    }

    init {
        runBlocking { refreshUsers() }
    }

    /**
     * Get a user from [USERS] by their [id].
     */
    fun getUser(id: Long): User? =
            USERS[id]

    /**
     * Get a user from [USERS] by their [username].
     */
    fun getUser(username: String): User? =
            USERS.values.singleOrNull { user -> user.username.equals(username, true) }

    /**
     * Insert into [USERS] a new [user].
     */
    fun insertUser(id: Long, user: User) {
        assert(user.id == id)
        USERS[id] = user
    }

    /**
     * Insert [pairs] into [USERS].
     */
    fun insertUsers(vararg pairs: Pair<Long, User>) {
        pairs.forEach { pair ->
            insertUser(pair.first, pair.second)
        }
    }

    /**
     * Remove [id] from [USERS].
     */
    fun removeUser(id: Long) {
        USERS.remove(id)
    }

    /**
     * If [USERS] contains a user with the [id].
     */
    fun hasUser(id: Long): Boolean =
            USERS.containsKey(id)

    /** If [USERS] contains a user with the [username] */
    fun hasUser(username: String): Boolean =
            USERS.values.singleOrNull { user -> user.username.equals(username, true) } != null
}