package dev.shog.mojor.api.blog

import dev.shog.mojor.api.RandomEmote
import org.json.JSONArray
import java.util.*

interface Blog

/**
 * A blog.
 *
 * TODO Improve tags
 */
data class DefaultBlog(
        val title: String,
        val body: String,
        val id: UUID,
        val creator: UUID,
        val tags: MutableList<String>,
        val date: Long
) : Blog

/**
 * A blog with emote parsing in the body.
 */
class EmoteBlog(val blog: DefaultBlog, val body: String) : Blog {
    companion object {
        /**
         * Add emotes into a [blog].
         */
        suspend fun fromBlog(blog: DefaultBlog): EmoteBlog {
            val emoteRegex = Regex(":\\w+:")
            var body = blog.body

            emoteRegex.findAll(blog.body)
                    .forEach { emote ->
                        val pureEmote = emote.value.removeSurrounding(":")
                        val parsedEmote = RandomEmote.getEmote(pureEmote)

                        if (parsedEmote != null) {
                            body = body.replaceFirst(
                                    emote.value,
                                    "<img " +
                                            "src=\"https:${parsedEmote.icon}\" " +
                                            "alt=\"${pureEmote}\" " +
                                            "height=\"${parsedEmote.height}\" " +
                                            "width=\"${parsedEmote.width}\" " +
                                            "/>"
                            )
                        }
                    }

            return EmoteBlog(blog, body)
        }
    }
}