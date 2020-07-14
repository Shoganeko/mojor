package dev.shog.mojor.api.users.game

/**
 * Holds [GameRecord]s
 */
data class GameRecordHolder(val lastRefresh: Long, val records: MutableList<GameRecord>)