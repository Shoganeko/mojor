package dev.shog.mojor.motd

import dev.shog.mojor.auth.user.UserHolder
import java.time.Instant
import java.util.*

/**
 * A motd class
 */
data class Motd(val data: String, val owner: Long, val date: Long) {
    /**
     * TODO Do markdown
     */
    fun getProperData() =
            data

    /**
     * Get [date] as a [Date]
     */
    fun getProperDate(): Date =
            Date.from(Instant.ofEpochMilli(date))

    /**
     * Get the name of [owner].
     */
    fun getOwnerName(): String =
            UserHolder.getUser(owner)?.username ?: "admin"
}