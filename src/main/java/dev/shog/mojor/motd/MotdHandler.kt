package dev.shog.mojor.motd

import dev.shog.mojor.db.PostgreSql
import reactor.core.publisher.Mono

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
    fun getMotdByDate(date: Long): Motd? {
        motds.forEach { motd ->
            if (motd.date == date)
                return motd
        }

        return null
    }

    /**
     * Insert a motd class into the database and [motds]
     */
    fun insertMotd(properMotd: Motd): Mono<Void> =
            PostgreSql.monoConnection()
                    .map { sql -> sql.prepareStatement("INSERT INTO motd.motds (data, owner, date) VALUES (?, ?, ?) ") }
                    .doOnNext { pre -> pre.setString(1, properMotd.data) }
                    .doOnNext { pre -> pre.setLong(2, properMotd.owner) }
                    .doOnNext { pre -> pre.setLong(3, properMotd.date) }
                    .map { pre -> pre.executeUpdate() }
                    .doFinally { motds.add(properMotd) }
                    .then()

    init {
        PostgreSql.monoConnection()
                .map { sql -> sql.prepareStatement("SELECT * FROM motd.motds") }
                .map { pre -> pre.executeQuery() }
                .subscribe { rs ->
                    while (rs.next()) {
                        motds.add(Motd(rs.getString("data"), rs.getLong("owner"), rs.getLong("date")))
                    }
                }
    }
}