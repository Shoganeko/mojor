package dev.shog.mojor.motd

import dev.shog.mojor.auth.user.UserHolder
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.time.Instant
import java.util.*

/**
 * A motd class
 */
data class Motd(val data: String, val owner: Long, val date: Long) {
    /**
     * Get the markdown rendered motd.
     */
    fun getProperData(): String {
        val document = Parser
                .builder()
                .build()
                .parse(data)

        val result = HtmlRenderer
                .builder()
                .build()
                .render(document)

        return result
                .substring(3, result.length - 5) // Cut off the <p></p> because that messes stuff up
    }

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