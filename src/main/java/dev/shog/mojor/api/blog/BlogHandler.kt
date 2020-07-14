package dev.shog.mojor.api.blog

import com.fasterxml.uuid.Generators
import dev.shog.lib.util.currentTimeMillis
import dev.shog.mojor.api.RandomEmote
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.db.PostgreSql
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.json.JSONArray
import java.sql.ResultSet
import java.util.*

object BlogHandler {
    private val cache = refreshCache()

    /**
     * Get a list of all blogs.
     */
    private fun refreshCache(): MutableList<Blog> {
        val list = mutableListOf<Blog>()

        val rs = PostgreSql.getConnection("Refresh blog cache")
                .prepareStatement("SELECT * FROM blogs.blogs")
                .executeQuery()

        while (rs.next())
            list.add(getBlog(rs))

        return list
    }

    /**
     * Get blogs from the database.
     */
    fun getBlogs(): MutableList<Blog> =
            cache

    /**
     * Get a cached blog by [id].
     */
    private fun getCachedBlogById(id: UUID): Blog? =
            cache.singleOrNull { blog -> blog.id == id }

    /**
     * Get cached blogs by author by [author].
     */
    private fun getCachedBlogsByAuthor(author: UUID): MutableList<Blog> =
            cache.filter { blog -> blog.creator == author }.toMutableList()

    /**
     * Get a blog by it's [id].
     */
    @Throws(NotFound::class)
    fun getBlogById(id: UUID): Blog =
            getCachedBlogById(id) ?: throw NotFound("blog")

    /**
     * Get blogs by author.
     */
    fun getBlogsByAuthor(author: UUID): MutableList<Blog> =
            getCachedBlogsByAuthor(author)

    /**
     * Get a body with emotes with <img> tags.
     */
    private suspend fun parseEmotes(blogBody: String): String {
        val emoteRegex = Regex(":\\w+:")
        var body = blogBody

        emoteRegex.findAll(blogBody)
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

        return body
    }

    /**
     * Create a blog.
     */
    suspend fun createBlog(author: UUID, title: String, body: String, tags: JSONArray): Blog {
        val id = Generators.randomBasedGenerator().generate()
        val parser = Parser.builder().build()
        val html = HtmlRenderer.builder().escapeHtml(true).build()

        val markdownBody = html.render(parser.parse(body))
                .removePrefix("<p>")
                .removeSuffix("</p>")

        val blog = Blog(id, author, currentTimeMillis(), title, parseEmotes(markdownBody), tags.toString())

        PostgreSql.getConnection("Create blog with $id")
                .prepareStatement("INSERT INTO blogs.blogs (id, title, body, creator, date, tags) VALUES (?, ?, ?, ?, ?, ?)")
                .apply {
                    setString(1, id.toString())
                    setString(2, title)
                    setString(3, markdownBody)
                    setString(4, author.toString())
                    setLong(5, blog.date)
                    setString(6, tags.toString())
                }
                .executeUpdate()

        cache.add(blog)

        return blog
    }

    /**
     * Delete a blog
     */
    fun deleteBlog(id: UUID) {
        cache.removeIf { blog -> blog.id == id }

        PostgreSql.getConnection("Delete a blog with $id")
                .prepareStatement("DELETE FROM blogs.blogs WHERE id = ?")
                .apply { setString(1, id.toString()) }
                .executeUpdate()
    }

    /**
     * Turn a [ResultSet] into a [Blog].
     */
    private fun getBlog(rs: ResultSet): Blog =
            Blog(
                    getUuid(rs.getString("id")),
                    getUuid(rs.getString("creator")),
                    rs.getLong("date"),
                    rs.getString("title"),
                    rs.getString("body"),
                    rs.getString("tags")
            )
}