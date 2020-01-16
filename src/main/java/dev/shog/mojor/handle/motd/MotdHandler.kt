package dev.shog.mojor.handle.motd

import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * The MOTD
 */
object MotdHandler {
    /**
     * All motds
     */
    val motds = ArrayList<Motd>()

    /**
     * Get the recent motd.
     */
    fun getMostRecentMotd(): Motd =
            motds.last()

    /**
     * Get the oldest motd.
     */
    fun getOldestMotd(): Motd =
            motds.first()

    /**
     * Get a [Motd] by their [date].
     */
    fun getMotdByDate(date: Long): Motd? =
            motds
                    .filter { motd -> motd.date == date }
                    .getOrNull(0)

    /**
     * Insert a motd class into the database and [motds]
     */
    suspend fun insertMotd(properMotd: Motd) = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("INSERT INTO motd.motds (data, owner, date) VALUES (?, ?, ?)")

        pre.setString(1, properMotd.data)
        pre.setLong(2, properMotd.owner)
        pre.setLong(3, properMotd.date)

        motds.add(properMotd)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.executeUpdate() }
    }

    init {
        runBlocking {
            val rs = withContext(Dispatchers.Unconfined) {
                PostgreSql.createConnection()
                        .prepareStatement("SELECT * FROM motd.motds")
                        .executeQuery()
            }

            while (rs.next()) {
                motds.add(Motd(rs.getString("data"), rs.getLong("owner"), rs.getLong("date")))
            }
        }
    }
}