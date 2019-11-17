package dev.shog.mojor.auth.user

import dev.shog.mojor.auth.ObjectPermissions
import dev.shog.mojor.db.PostgreSql
import org.json.JSONArray
import java.util.concurrent.ConcurrentHashMap

/**
 * Holds users.
 */
object UserHolder {
    val USERS = ConcurrentHashMap<Long, User>()

    /**
     * Get all users from the database and insert it into the map.
     */
    init {
        PostgreSql
                .monoConnection()
                .map { sql -> sql.prepareStatement("SELECT * FROM users.users") }
                .map { pre -> pre.executeQuery() }
                .subscribe { rs ->
                    while (rs.next()) {
                        USERS[rs.getLong("id")] = User(
                                rs.getString("name"),
                                rs.getString("password"),
                                rs.getLong("id"),
                                ObjectPermissions.fromJsonArray(
                                        JSONArray(rs.getString("permissions"))
                                ),
                                rs.getLong("createdon")
                        )
                    }
                }
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