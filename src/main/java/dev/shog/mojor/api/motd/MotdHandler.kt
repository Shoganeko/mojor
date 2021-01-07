package dev.shog.mojor.api.motd

import com.mongodb.client.model.Filters
import dev.shog.mojor.handle.db.Mongo
import org.bson.Document
import java.util.*

/**
 * The MOTD
 */
object MotdHandler {
    /**
     * All motds
     */
    val motds: MutableList<Motd> by lazy {
        Mongo.getClient()
            .getDatabase("motd")
            .getCollection("motd")
            .find()
            .map { doc ->
                Motd(
                    doc.getString("data"),
                    UUID.fromString(doc.getString("owner")),
                    doc.getLong("date")
                )
            }
            .toMutableList()
    }

    /**
     * Get a [Motd] by their [date].
     */
    fun getMotdByDate(date: Long): Motd? =
        motds.singleOrNull { motd -> motd.date == date }

    /**
     * Insert a motd class into the database and [motds]
     */
    fun insertMotd(motd: Motd) {
        motds.add(motd)

        Mongo.getClient()
            .getDatabase("motd")
            .getCollection("motd")
            .insertOne(
                Document(
                    mapOf(
                        "data" to motd.data,
                        "owner" to motd.owner.toString(),
                        "date" to motd.date
                    )
                )
            )
    }

    /**
     * @param date The date the MOTD was created.
     */
    fun deleteMotd(date: Long) {
        motds.removeIf { it.date == date }

        Mongo.getClient()
            .getDatabase("motd")
            .getCollection("motd")
            .deleteOne(Filters.eq("date", date))
    }
}