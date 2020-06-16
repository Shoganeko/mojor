package dev.shog.mojor.api.blog

import com.fasterxml.uuid.Generators
import dev.shog.mojor.api.blog.response.BlogResponse
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.AlreadyExists
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.db.PostgreSql
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.json.JSONArray
import org.jsoup.Jsoup
import java.sql.ResultSet
import java.util.*

/**
 * TODO cache.
 */
object BlogHandler {
    /**
     * Get blogs from the database.
     */
    fun getBlogs(): MutableList<DefaultBlog> {
        val list = mutableListOf<DefaultBlog>()

        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM blogs.blogs")
                .executeQuery()

        while (rs.next())
            list.add(getBlog(rs))

        return list
    }

    /**
     * Get a blog by it's [id].
     */
    @Throws(NotFound::class)
    fun getBlogById(id: UUID): DefaultBlog {
        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM blogs.blogs WHERE id = ?")
                .apply { setString(1, id.toString()) }
                .executeQuery()

        if (rs.next())
            return getBlog(rs)
        else throw NotFound("blog")
    }

    /**
     * Get blogs by author.
     */
    fun getBlogsByAuthor(author: UUID): MutableList<DefaultBlog> {
        val list = mutableListOf<DefaultBlog>()

        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT * FROM blogs.blogs WHERE creator = ?")
                .apply { setString(1, author.toString()) }
                .executeQuery()

        while (rs.next())
            list.add(getBlog(rs))

        return list
    }

    /**
     * Create a blog.
     */
    fun createBlog(author: UUID, title: String, body: String, tags: JSONArray): DefaultBlog {
        val id = Generators.randomBasedGenerator().generate()

        val parser = Parser
                .builder()
                .build()

        val html = HtmlRenderer
                .builder()
                .escapeHtml(true)
                .build()

        val markdownBody = html.render(parser.parse(body))
                .removePrefix("<p>")
                .removeSuffix("</p>")

        val blog = DefaultBlog(
                title,
                markdownBody,
                id,
                author,
                tags
                        .toList()
                        .map { it.toString() }
                        .toMutableList(),
                System.currentTimeMillis()
        )

        PostgreSql.getConnection()
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

        return blog
    }

    /**
     * Delete a blog
     */
    fun deleteBlog(id: UUID) {
        PostgreSql.getConnection()
                .prepareStatement("DELETE FROM blogs.blogs WHERE id = ?")
                .apply { setString(1, id.toString()) }
                .executeUpdate()
    }

    /**
     * Add tags.
     */
    @Throws(NotFound::class, AlreadyExists::class)
    fun addTag(id: UUID, tag: String) {
        val tags = getTags(id)

        if (!tags.contains(tag)) {
            tags.put(tag)

            PostgreSql.getConnection()
                    .prepareStatement("UPDATE blogs.blogs SET tags = ? WHERE id = ?")
                    .apply {
                        setString(1, tags.toString())
                        setString(2, id.toString())
                    }
                    .executeUpdate()
        } else throw AlreadyExists(tag)
    }

    /**
     * Get a blog's tags.
     */
    @Throws(NotFound::class)
    private fun getTags(id: UUID): JSONArray {
        val rs = PostgreSql.getConnection()
                .prepareStatement("SELECT tags FROM blogs.blogs WHERE id = ?")
                .apply { setString(1, id.toString()) }
                .executeQuery()

        if (rs.next())
            return JSONArray(rs.getString("tags"))
        else throw NotFound("blog")
    }

    /**
     * Remove tags.
     */
    @Throws(NotFound::class)
    fun removeTag(id: UUID, tag: String) {
        val tags = getTags(id)

        if (tags.contains(tag)) {
            val index = tags.indexOf(tag)
            tags.remove(index)

            PostgreSql.getConnection()
                    .prepareStatement("UPDATE blogs.blogs SET tags = ? WHERE id = ?")
                    .apply {
                        setString(1, tags.toString())
                        setString(2, id.toString())
                    }
                    .executeUpdate()
        } else throw NotFound("tag")
    }

    /**
     * Turn a [ResultSet] into a [Blog].
     */
    private fun getBlog(rs: ResultSet): DefaultBlog =
            DefaultBlog(
                    rs.getString("title"),
                    rs.getString("body"),
                    getUuid(rs.getString("id"))!!,
                    getUuid(rs.getString("creator"))!!,
                    JSONArray(rs.getString("tags"))
                            .toList()
                            .map { it.toString() }
                            .toMutableList(),
                    rs.getLong("date")
            )
}