package dev.shog.mojor.api.blog

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import dev.shog.mojor.handle.db.Mongo
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
        tags: List<String>
) {
    /**
     * The blog's title.
     */
    var title = title
        set(value) {
            Mongo.getClient()
                    .getDatabase("blogs")
                    .getCollection("blogs")
                    .updateOne(Filters.eq("id", id), Updates.set("title", value))

            field = value
        }

    /**
     * The blog's body.
     */
    var body = body
        set(value) {
            Mongo.getClient()
                    .getDatabase("blogs")
                    .getCollection("blogs")
                    .updateOne(Filters.eq("id", id), Updates.set("body", value))

            field = value
        }


    /**
     * The tags.
     */
    var tags = tags
        set(value) {
            Mongo.getClient()
                    .getDatabase("blogs")
                    .getCollection("blogs")
                    .updateOne(Filters.eq("id", id), Updates.set("tags", value))

            field = value
        }
}