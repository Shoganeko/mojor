package dev.shog.mojor.api.users.handle

import dev.shog.mojor.api.users.obj.UserLoginAttempt
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.*

/**
 * Manages user login attempts.
 */
object UserLoginManager {
    private val cache: MutableList<UserLoginAttempt> by lazy { refreshCache() }

    /**
     * Get all login attempts.
     */
    private fun refreshCache(): MutableList<UserLoginAttempt> {
        val rs = PostgreSql.getConnection("Get all user login attempts")
                .prepareStatement("SELECT id, ip, date, success FROM users.attempts")
                .executeQuery()

        val list = mutableListOf<UserLoginAttempt>()

        while (rs.next()) {
            list.add(UserLoginAttempt(
                    UUID.fromString(rs.getString("id")),
                    rs.getString("ip"),
                    rs.getLong("date"),
                    rs.getBoolean("success")
            ))
        }

        return list
    }

    /**
     * Attempt a login for [id].
     */
    suspend fun attemptLogin(id: UUID, ip: String, success: Boolean): UserLoginAttempt {
        val time = System.currentTimeMillis()

        withContext(Dispatchers.Unconfined) {
            PostgreSql.getConnection()
                    .prepareStatement("INSERT INTO users.attempts (id, ip, success, date) VALUES (?, ?, ?, ?)")
                    .apply {
                        setString(1, id.toString())
                        setString(2, ip)
                        setBoolean(3, success)
                        setLong(4, time)
                    }
                    .executeUpdate()
        }

        val attempt = UserLoginAttempt(id, ip, time, success)

        cache.add(attempt)

        return attempt
    }

    /**
     * Get [id]'s most recent login attempt.
     */
    fun getMostRecentLoginAttempt(id: UUID): UserLoginAttempt =
            cache.first { attempt -> attempt.id == id }

    /**
     * Get a user's login attempts
     */
    fun getLoginAttempts(id: UUID, limit: Int = 100): List<UserLoginAttempt> =
            cache
                    .filter { attempt -> attempt.id == id }
                    .take(limit)
}