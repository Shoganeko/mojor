package dev.shog.mojor.handle.auth.user.handle

import dev.shog.mojor.handle.auth.user.obj.UserLoginAttempt
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Manages user login attempts.
 */
object UserLoginManager {
    /**
     * Attempt a login for [id].
     */
    suspend fun attemptLogin(id: UUID, ip: String, success: Boolean): UserLoginAttempt {
        val time = System.currentTimeMillis()

        withContext(Dispatchers.Unconfined) {
            PostgreSql.getConnection()
                    .prepareStatement("INSERT INTO users.signin (id, ip, success, date) VALUES (?, ?, ?, ?)")
                    .apply {
                        setString(1, id.toString())
                        setString(2, ip)
                        setBoolean(3, success)
                        setLong(4, time)
                    }
                    .executeUpdate()
        }

        return UserLoginAttempt(id, ip, time, success)
    }

    /**
     * Get [id]'s most recent login attempt.
     */
    suspend fun getMostRecentLoginAttempt(id: UUID): UserLoginAttempt {
        val rs = withContext(Dispatchers.Unconfined) {
            PostgreSql.getConnection()
                    .prepareStatement("SELECT ip, date, success FROM users.signin WHERE id = ? LIMIT 1")
                    .apply { setString(1, id.toString()) }
                    .executeQuery()
        }

        if (rs.next()) {
            return UserLoginAttempt(id, rs.getString("ip"), rs.getLong("date"), rs.getBoolean("success"))
        } else throw Exception("Could not find a most recent login attempt!")
    }

    /**
     * Get a user's login attempts
     */
    suspend fun getLoginAttempts(id: UUID, limit: Int = 100): List<UserLoginAttempt> {
        val rs = withContext(Dispatchers.Unconfined) {
            PostgreSql.getConnection()
                    .prepareStatement("SELECT ip, date, success FROM users.signin WHERE id = ? LIMIT ?")
                    .apply {
                        setString(1, id.toString())
                        setInt(2, limit)
                    }
                    .executeQuery()
        }

        val attempts = mutableListOf<UserLoginAttempt>()

        while (rs.next()) {
            attempts.add(UserLoginAttempt(id, rs.getString("ip"), rs.getLong("date"), rs.getBoolean("success")))
        }

        return attempts
    }
}