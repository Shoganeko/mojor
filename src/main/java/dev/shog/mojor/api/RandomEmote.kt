package dev.shog.mojor.api

/**
 * TODO move this to a file.
 */
object RandomEmote {
    /**
     * The available emotes.
     */
    private val emotes = arrayListOf(
            "PogU", "Pog", "PogChamp", "xqcP", "xqcM", "squadW", "POGGERS", "Pepega", "Clap"
    )

    /**
     * Get a random emote
     */
    fun getEmote(): String = emotes.random()
}