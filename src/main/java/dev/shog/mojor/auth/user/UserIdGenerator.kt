package dev.shog.mojor.auth.user

import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.random.Random

/**
 * Generates an unused user ID.
 * This ID is a [Long] that is 18 characters long.
 */
object UserIdGenerator {
    /** Create a 18 character long user ID */
    private fun createId(): Long {
        var preId = ""
        (0..17).forEach { _ ->
            preId += Random.nextInt(10)
        }

        return preId.toLong()
    }

    /**
     * Get an unused ID.
     */
    suspend fun getNewId(): Long {
        var id = createId()

        while (checkExists(id)) {
            id = createId()
        }

        return id
    }

    /** Check if [id] is an already used ID */
    private suspend fun checkExists(id: Long): Boolean = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("SELECT name FROM users.users WHERE ID = ?")

        pre.setLong(1, id)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.executeQuery().fetchSize != 0 }
    }
}