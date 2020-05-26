package dev.shog.mojor.handle.game

import dev.shog.lib.util.getAge
import dev.shog.mojor.handle.db.PostgreSql
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

/**
 * Manages /@{user}/{game}
 */
object GameHandler {
    private val CACHE = ConcurrentHashMap<UUID, GameRecordHolder>()

    /**
     * Force refresh a user's game record after one hour.
     */
    private val FORCE_REFRESH_AFTER = TimeUnit.HOURS.toMillis(1)

    /**
     * Upload [user]'s record.
     */
    suspend fun uploadUserRecord(user: UUID, game: Int, win: Short, score: String, map: String, date: Long = System.currentTimeMillis()) {
        PostgreSql.createConnection()
                .prepareStatement("INSERT INTO users.games (id, game, win, score, map, date) VALUES (?, ?, ?, ?, ?, ?)")
                .apply {
                    setString(1, user.toString())
                    setInt(2, game)
                    setShort(3, win)
                    setString(4, score)
                    setString(5, map)
                    setLong(6, date)
                }
                .executeUpdate()

        val record = GameRecord(user, game, win, score, map, date)

        CACHE[user]?.records?.add(record) // only adds if it exists, otherwise it'll be retrieved from db
    }

    /**
     * Delete a [user]'s record using a [date].
     */
    suspend fun removeUserRecord(user: UUID, date: Long) {
        PostgreSql.createConnection()
                .prepareStatement("DELETE FROM users.games WHERE id=? & date=?")
                .apply {
                    setString(1, user.toString())
                    setLong(2, date)
                }
                .executeUpdate()

        val record = CACHE[user]?.records?.filter { it -> it.date == date }

        if (record != null)
            CACHE[user]?.records?.remove(record.single())
    }

    /**
     * Get [user]'s game record.
     */
    suspend fun getUserGameRecord(user: UUID): GameRecordHolder {
        val userCache = CACHE[user]

        return when {
            userCache == null ->
                refreshUserRecords(user)

            userCache.lastRefresh.getAge() > FORCE_REFRESH_AFTER ->
                refreshUserRecords(user)

            else -> userCache
        }
    }

    /**
     * Refresh [user]'s record and return the new record.
     */
    private suspend fun refreshUserRecords(user: UUID): GameRecordHolder {
        val rs = PostgreSql
                .createConnection()
                .prepareStatement("SELECT * FROM users.games WHERE id=?")
                .apply { setString(1, user.toString()) }
                .executeQuery()

        val gameRecord = mutableListOf<GameRecord>()

        while (rs.next()) {
            val record = GameRecord(user, rs.getInt("game"), rs.getShort("win"), rs.getString("score"), rs.getString("map"), rs.getLong("date"))

            gameRecord.add(record)
        }

        val holder = GameRecordHolder(System.currentTimeMillis(), gameRecord)

        CACHE[user] = holder

        return holder
    }
}