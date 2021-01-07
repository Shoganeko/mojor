package dev.shog.mojor.api.users.handle

import dev.shog.mojor.api.users.obj.UserLoginAttempt
import dev.shog.mojor.handle.db.Mongo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.bson.Document
import java.util.*

/**
 * Manages user login attempts.
 */
object UserLoginManager {
    private val cache: MutableList<UserLoginAttempt> by lazy {
        Mongo.getClient()
            .getDatabase("users")
            .getCollection("attempts")
            .find()
            .map { doc ->
                UserLoginAttempt(
                    UUID.fromString(doc.getString("id")),
                    doc.getString("ip"),
                    doc.getLong("date"),
                    doc.getBoolean("success")
                )
            }
            .toMutableList()
    }

    /**
     * Attempt a login for [id].
     */
    suspend fun attemptLogin(id: UUID, ip: String, success: Boolean): UserLoginAttempt {
        val time = System.currentTimeMillis()

        withContext(Dispatchers.Unconfined) {
            Mongo.getClient()
                .getDatabase("users")
                .getCollection("attempts")
                .insertOne(
                    Document(
                        mapOf(
                            "id" to id.toString(),
                            "ip" to ip,
                            "success" to success,
                            "date" to time
                        )
                    )
                )
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