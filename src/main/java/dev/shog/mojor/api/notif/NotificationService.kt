package dev.shog.mojor.api.notif

import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.codec.digest.DigestUtils
import org.postgresql.util.PSQLException
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

object NotificationService {
    /**
     * The saved notifications.
     */
    private val saved = ConcurrentHashMap<UUID, MutableList<Notification>>()

    /**
     * Get notifications for a user.
     *
     * @param id The ID for the user.
     * @param forceRefresh Don't use [saved] and use the database.
     * @return A list of notifications for the user.
     */
    suspend fun getNotificationsForUser(id: UUID, forceRefresh: Boolean = false): MutableList<Notification> =
            if (forceRefresh || saved[id].isNullOrEmpty())
                getNotificationsUsingDatabase(id)
            else saved[id] ?: mutableListOf()

    /**
     * Create an ID for a notification.
     *
     * @return an unused ID
     */
    private suspend fun createId(): String = coroutineScope {
        val id = DigestUtils.md5Hex(String(Random.nextBytes(64)))
        val pre = PostgreSql.createConnection()
                .prepareStatement("SELECT * FROM notif.notif WHERE id = ?")

        pre.setString(1, id)
        val rs = withContext(Dispatchers.Unconfined) { pre.executeQuery() }

        when {
            rs.next() -> return@coroutineScope createId()
            else -> return@coroutineScope id
        }
    }

    /**
     * Post a notification for a user.
     *
     * @param data The data for inside of the notification.
     * @param forUser For what user.
     */
    suspend fun postNotification(data: String, forUser: UUID) = coroutineScope {
        val notif = Notification(data, System.currentTimeMillis(), createId(), forUser.toString())

        val saved = saved[forUser]

        if (saved == null)
            this@NotificationService.saved[forUser] = arrayListOf(notif)
        else {
            saved.add(notif)
            this@NotificationService.saved[forUser] = saved
        }

        val pre = PostgreSql.createConnection()
                .prepareStatement("INSERT INTO notif.notif (postedat, id, data, intended) VALUES (?, ?, ?, ?)")

        pre.setLong(1, notif.postedAt)
        pre.setString(2, notif.id)
        pre.setString(3, notif.data)
        pre.setString(4, notif.intendedFor)

        launch { pre.executeUpdate() }
    }

    /**
     * Get notifications from the database.
     *
     * @param [id] The user's ID
     * @return The list of notifications.
     */
    private suspend fun getNotificationsUsingDatabase(id: UUID): MutableList<Notification> = coroutineScope {
        val rs = PostgreSql.createConnection()
                .prepareStatement("SELECT * FROM notif.notif WHERE intended = ?")
                .apply { setString(1, id.toString()) }
                .executeQuery()

        val list = mutableListOf<Notification>()

        while (rs.next()) {
            list.add(Notification(
                    rs.getString("data"),
                    rs.getLong("postedAt"),
                    rs.getString("id"),
                    rs.getString("intended")
            ))
        }

        saved[id] = list

        return@coroutineScope list
    }

    /**
     * A notification.
     *
     * @param data The contents of the notification
     * @param postedAt The millisecond epoch where it was posted.
     * @param id The ID of the notification.
     * @param intendedFor Who it's intended for.
     */
    data class Notification(val data: String = "", val postedAt: Long = -1L, val id: String = "", val intendedFor: String = "") {
        /**
         * Delete the notification.
         */
        suspend fun close() = coroutineScope {
            closeNotification(id, UUID.fromString(intendedFor))
        }
    }

    /**
     * Close a notification.
     *
     * @param id The notification ID.
     * @param intendedFor The intended user for the notification. This insures that only the owner can delete the notification.
     */
    suspend fun closeNotification(id: String, intendedFor: UUID) = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("DELETE FROM notif.notif WHERE id = ? and intended = ?")

        pre.setString(1, id)
        pre.setString(2, intendedFor.toString())

        val intended = saved[intendedFor]

        if (intended != null) {
            val value = intended
                    .single { notif -> notif.id == id }

            intended.remove(value)

            saved[intendedFor] = intended
        }

        launch { pre.executeUpdate() }
    }
}