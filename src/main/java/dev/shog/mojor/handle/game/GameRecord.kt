package dev.shog.mojor.handle.game

import java.util.*

/**
 * A single game record for [user].
 *
 * @param game The game. [csgo] or [overwatch]
 * @param win 1 = win, 0 = loss
 * @param score The score of the game in x-x format.
 * @param map The map the game was played on.
 * @param date When the game took place.
 */
data class GameRecord(
        val user: UUID,
        val game: Int,
        val win: Short,
        val score: String,
        val map: String,
        val date: Long
) {
    companion object {
        const val csgo = 1
        const val overwatch = 2
        const val valorant = 3
    }

    /**
     * Get [game] as a string.
     */
    fun getGameAsString(): String {
        return when (game) {
            csgo -> "CS:GO"
            valorant -> "Valorant"
            overwatch -> "Overwatch"

            else -> "Invalid Game"
        }
    }
}