package dev.shog.mojor.api.blog

import com.fasterxml.uuid.Generators
import com.mongodb.client.model.Filters
import dev.shog.lib.util.currentTimeMillis
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.db.Mongo
import org.bson.Document
import java.sql.ResultSet
import java.util.*

object BlogHandler {
    private val cache by lazy {
        Mongo.getClient()
                .getDatabase("blogs")
                .getCollection("blogs")
                .find()
                .map { blog -> getBlog(blog) }
                .toMutableList()
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
     * Create a blog.
     */
    fun createBlog(author: UUID, title: String, body: String, tags: List<String>): Blog {
        val id = Generators.randomBasedGenerator().generate()

        val blog = Blog(id, author, currentTimeMillis(), title, body, tags)

        Mongo.getClient()
                .getDatabase("blogs")
                .getCollection("blogs")
                .insertOne(Document(mapOf(
                        "id" to id.toString(),
                        "title" to title,
                        "body" to body,
                        "creator" to author.toString(),
                        "date" to blog.date,
                        "tags" to blog.tags
                )))

        cache.add(blog)

        return blog
    }

    /**
     * Delete a blog
     */
    fun deleteBlog(id: UUID) {
        cache.removeIf { blog -> blog.id == id }

        Mongo.getClient()
                .getDatabase("blogs")
                .getCollection("blogs")
                .deleteOne(Filters.eq("id", id.toString()))
    }

    /**
     * Turn a [ResultSet] into a [Blog].
     */
    private fun getBlog(bson: Document): Blog =
            Blog(
                    getUuid(bson.getString("id")),
                    getUuid(bson.getString("creator")),
                    bson.getLong("date"),
                    bson.getString("title"),
                    bson.getString("body"),
                    bson["tags"] as List<String>
            )
}