package dev.shog.mojor.handle.game

/**
 * Holds [GameRecord]s
 */
data class GameRecordHolder(val lastRefresh: Long, val records: MutableList<GameRecord>)