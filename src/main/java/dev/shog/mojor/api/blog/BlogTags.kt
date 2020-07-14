package dev.shog.mojor.api.blog

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.handle.db.PostgreSql
import org.json.JSONArray
import java.util.*
import kotlin.collections.ArrayList

/**
 * A blog's tags. Tags are represented by a JSONArray.
 *
 * @param id The ID of the blog.
 * @param tags The JSONArray string of the tags.
 */
class BlogTags(
        @JsonIgnore
        val id: UUID,
        tags: String
) : ArrayList<String>() {
    init {
        val mapper = ObjectMapper()

        addAll(mapper.readValue(tags, mapper.typeFactory.constructCollectionType(
                ArrayList::class.java,
                String::class.java)
        ))
    }

    /**
     * Add [tag] to the JSONArray but also in the database.
     */
    fun addTag(tag: String): ArrayList<String> {
        super.add(tag)
        update()
        return this
    }

    /**
     * Remove [tag] from the JSONArray and the database.
     */
    fun removeTag(tag: String): ArrayList<String> {
        super.remove(tag)
        update()
        return this
    }

    /**
     * Update the current state to the database.
     */
    private fun update() {
        PostgreSql.getConnection("Update blog tags for $id")
                .prepareStatement("UPDATE blogs.blogs SET tags = ? WHERE id = ?")
                .apply {
                    setString(1, ObjectMapper().writeValueAsString(this))
                    setString(2, id.toString())
                }
                .executeUpdate()
    }
}