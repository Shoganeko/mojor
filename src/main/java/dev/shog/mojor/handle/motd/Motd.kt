package dev.shog.mojor.handle.motd

import dev.shog.lib.util.asDate
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.handle.MarkdownModifier
import dev.shog.mojor.handle.modify
import java.util.*

/**
 * A motd class
 */
data class Motd(val data: String, val owner: Long, val date: Long) {
    /**
     * Get the markdown rendered motd.
     */
    fun getProperData(): String {
        val result = data modify MarkdownModifier

        return result
                .substring(3, result.length - 5) // Cut off the <p></p> because that messes stuff up
    }

    /**
     * Get [date] as a [Date]
     */
    fun getProperDate(): Date =
            date.asDate()

    /**
     * Get the name of [owner].
     */
    fun getOwnerName(): String =
            UserHolder.getUser(owner)?.username ?: "admin"
}