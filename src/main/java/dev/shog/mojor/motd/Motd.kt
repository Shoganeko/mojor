package dev.shog.mojor.motd

import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.db.PostgreSql
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*
import kotlin.collections.ArrayList

/**
 * The MOTD
 */
object Motd {
    /**
     * All motds
     */
    private val motds = ArrayList<MotdClass>()

    /**
     * A motd class
     */
    data class MotdClass(val data: String, val owner: Long, val date: Long) {
        /**
         * TODO Do markdown
         */
        fun getProperData() = data

        /**
         * Get [date] as a [Date]
         */
        fun getProperDate(): Date = Date.from(Instant.ofEpochMilli(date))

        /**
         * Get the name of [owner].
         */
        fun getOwnerName(): String = UserHolder.getUser(owner)?.username ?: "admin"
    }

    /**
     * The most recent MOTD
     */
    private var mostRecentMotd: MotdClass? = null

    /**
     * Get the recent motd.
     */
    fun getMostRecentMotd(): MotdClass =
            mostRecentMotd ?: refreshMostRecentMotd()

    /**
     * Refresh [mostRecentMotd]
     */
    private fun refreshMostRecentMotd(): MotdClass {
        var mostRecent: MotdClass? = null
        var mostRecentDif: Long? = null

        motds.forEach { motd ->
            val dif = System.currentTimeMillis() - motd.date

            mostRecentDif.also { mrd ->
                if (mrd != null) {
                    if (mrd > dif) {
                        mostRecentDif = dif
                        mostRecent = motd
                    }
                } else {
                    mostRecentDif = dif
                    mostRecent = motd
                }
            }
        }

        mostRecentMotd = mostRecent!!

        return mostRecent!!
    }

    /**
     * Insert a motd class into the database and [motds]
     */
    fun insertMotd(motdClass: MotdClass): Mono<Void> =
            PostgreSql.monoConnection()
                    .map { sql -> sql.prepareStatement("INSERT INTO motd.motds (data, owner, date) VALUES (?, ?, ?) ") }
                    .doOnNext { pre -> pre.setString(1, motdClass.data) }
                    .doOnNext { pre -> pre.setLong(2, motdClass.owner) }
                    .doOnNext { pre -> pre.setLong(3, motdClass.date) }
                    .map { pre -> pre.executeUpdate() }
                    .doFinally {
                        motds.add(motdClass)
                        refreshMostRecentMotd()
                    }
                    .then()

    init {
        PostgreSql.monoConnection()
                .map { sql -> sql.prepareStatement("SELECT * FROM motd.motds") }
                .map { pre -> pre.executeQuery() }
                .subscribe { rs ->
                    while (rs.next()) {
                        motds.add(MotdClass(rs.getString("data"), rs.getLong("owner"), rs.getLong("date")))
                    }

                    refreshMostRecentMotd()
                }
    }
}