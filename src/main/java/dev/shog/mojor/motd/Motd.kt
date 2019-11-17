package dev.shog.mojor.motd

import dev.shog.mojor.db.PostgreSql
import reactor.core.publisher.Mono

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
    data class MotdClass(val data: String, val owner: Long, val date: Long)

    /**
     * The most recent MOTD
     */
    private lateinit var mostRecentMotd: MotdClass

    /**
     * Get the recent motd.
     */
    fun getMostRecentMotd(): MotdClass =
            mostRecentMotd

    /**
     * Refresh [mostRecentMotd]
     */
    private fun refreshMostRecentMotd() {
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