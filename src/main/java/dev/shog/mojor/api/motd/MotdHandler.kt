package dev.shog.mojor.api.motd

import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import java.util.*
import javax.xml.ws.Dispatch
import kotlin.collections.ArrayList

/**
 * The MOTD
 */
object MotdHandler {
    /**
     * All motds
     */
    val motds = ArrayList<Motd>()

    /**
     * Get a [Motd] by their [date].
     */
    fun getMotdByDate(date: Long): Motd? =
            motds.singleOrNull { motd -> motd.date == date }

    /**
     * Insert a motd class into the database and [motds]
     */
    suspend fun insertMotd(properMotd: Motd) {
        motds.add(properMotd)

        PostgreSql.getConnection()
                .prepareStatement("INSERT INTO motd.motds (data, owner, date) VALUES (?, ?, ?)")
                .apply {
                    setString(1, properMotd.data)
                    setString(2, properMotd.owner.toString())
                    setLong(3, properMotd.date)
                }

        withContext(Dispatchers.Unconfined) {
            PostgreSql.getConnection()
                    .prepareStatement("INSERT INTO motd.motds (data, owner, date) VALUES (?, ?, ?)")
                    .apply {
                        setString(1, properMotd.data)
                        setString(2, properMotd.owner.toString())
                        setLong(3, properMotd.date)
                    }
                    .executeUpdate()
        }
    }

    /**
     * @param motdDate The date the MOTD was created.
     */
    suspend fun deleteMotd(motdDate: Long) =
            withContext(Dispatchers.Unconfined) {
                motds.removeIf { it.date == motdDate }

                PostgreSql.getConnection()
                        .prepareStatement("DELETE FROM motd.motds WHERE date = ?")
                        .apply { setLong(1, motdDate) }
                        .executeUpdate()
            }

    init {
        runBlocking {
            val rs = withContext(Dispatchers.Unconfined) {
                PostgreSql.getConnection()
                        .prepareStatement("SELECT * FROM motd.motds")
                        .executeQuery()
            }

            while (rs.next()) {
                motds.add(Motd(
                        rs.getString("data"),
                        UUID.fromString(rs.getString("owner")),
                        rs.getLong("date")
                ))
            }
        }
    }
}