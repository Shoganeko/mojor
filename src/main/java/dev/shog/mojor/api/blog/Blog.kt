package dev.shog.mojor.api.blog

import com.fasterxml.jackson.annotation.JsonIgnore
import dev.shog.mojor.api.RandomEmote
import dev.shog.mojor.handle.db.PostgreSql
import org.json.JSONArray
import java.util.*

/**
 * A blog.
 */
class Blog(
        val id: UUID,
        val creator: UUID,
        val date: Long,
        title: String,
        body: String,
        tags: String
) {
    /**
     * The blog's title.
     */
    var title = title
        set(value) {
            PostgreSql.getConnection("Set title for blog $id")
                    .prepareStatement("UPDATE blogs.blogs SET title = ? WHERE id = ?")
                    .apply {
                        setString(1, value)
                        setString(2, id.toString())
                    }
                    .executeUpdate()

            field = value
        }

    /**
     * The blog's body.
     */
    var body = body
        set(value) {
            PostgreSql.getConnection("Set body for blog $id")
                    .prepareStatement("UPDATE blogs.blogs SET body = ? WHERE id = ?")
                    .apply {
                        setString(1, value)
                        setString(2, id.toString())
                    }
                    .executeUpdate()

            field = value
        }


    /**
     * The tags.
     */
    var tags = BlogTags(id, tags)
}