package dev.shog.mojor.api.buta.data

import com.mongodb.client.model.Filters
import com.mongodb.client.model.Updates
import dev.shog.mojor.api.buta.bot.ButaInteraction
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.db.Mongo
import kotlin.reflect.KClass
import kotlin.reflect.cast

object ButaDataHandler {
    /**
     * Cached Buta guilds.
     */
    private val guildCache: MutableMap<Long, ButaGuild> by lazy {
        Mongo.getClient()
            .getDatabase("buta")
            .getCollection("guilds")
            .find()
            .map { doc ->
                doc.getLong("id") to ButaGuild(
                    doc.getLong("id").toString(),
                    doc.getString("prefix"),
                    doc.getString("join_message"),
                    doc.getString("leave_message"),
                    doc.getLong("join_role").toString(),
                    doc.getString("swear_filter_msg"),
                    doc["disabled_categories"] as List<String>,
                    doc.getBoolean("swear_filter_on")
                )
            }
            .toMap()
            .toMutableMap()
    }

    /**
     * Get a guild by it's [id] from the [guildCache]
     */
    @Throws(NotFound::class)
    fun getGuild(id: Long): ButaGuild =
        guildCache[id] ?: throw NotFound("buta_guild")

    /**
     * If [id] exists in the [guildCache]
     */
    fun exists(id: Long): Boolean =
        guildCache.containsKey(id)

    /**
     * Types that can be modified.
     */
    private val validTypes =
        hashMapOf("prefix" to "", "swear_filter_msg" to "", "swear_filter_on" to false, "join_role" to 0L)

    /**
     * Set an object in the database for guild [id].
     */
    @Throws(InvalidArguments::class, NotFound::class)
    suspend fun setObject(id: Long, type: String, value: String) {
        when {
            !exists(id) ->
                throw NotFound("buta_guild")

            !validTypes.contains(type) ->
                throw InvalidArguments("type")
        }

        val parsedType: Any = when (validTypes[type]!!) {
            is String -> value
            is Boolean -> value.toBoolean()
            is Int -> value.toInt()
            is Long -> value.toLong()
            is Float -> value.toFloat()
            is Double -> value.toDouble()

            else -> throw InvalidArguments("value")
        }

        Mongo.getClient()
            .getDatabase("buta")
            .getCollection("guilds")
            .updateOne(Filters.eq("id", id), Updates.set(type, parsedType))

        refreshObj(id)
    }

    private suspend fun refreshObj(id: Long) {
        ButaInteraction.refreshGuild(id)

        val guild = Mongo.getClient()
            .getDatabase("buta")
            .getCollection("guilds")
            .find(Filters.eq("id", id))
            .map { doc ->
                ButaGuild(
                    doc.getLong("id").toString(),
                    doc.getString("prefix"),
                    doc.getString("join_message"),
                    doc.getString("leave_message"),
                    doc.getLong("join_role").toString(),
                    doc.getString("swear_filter_msg"),
                    doc["disabled_categories"] as List<String>,
                    doc.getBoolean("swear_filter_on")
                )
            }
            .firstOrNull()
            ?: throw NotFound("guild")


        guildCache[guild.id.toLong()] = guild
    }
}