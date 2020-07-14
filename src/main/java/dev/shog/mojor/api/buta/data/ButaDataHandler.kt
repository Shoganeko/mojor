package dev.shog.mojor.api.buta.data

import com.fasterxml.jackson.databind.ObjectMapper
import dev.shog.mojor.api.buta.socket.ButaInteraction
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.db.PostgreSql
import java.util.concurrent.ConcurrentHashMap

object ButaDataHandler {
    private val guildCache: ConcurrentHashMap<Long, ButaGuild> by lazy {
        val rs = PostgreSql.getConnection("Get all ButaGuild")
                .prepareStatement("SELECT * FROM buta.guilds")
                .executeQuery()

        val mapper = ObjectMapper()
        val list = ConcurrentHashMap<Long, ButaGuild>()

        while (rs.next()) {
            val guild = ButaGuild(
                    rs.getLong("id").toString(),
                    rs.getString("prefix"),
                    rs.getString("join_message"),
                    rs.getString("leave_message"),
                    rs.getLong("join_role").toString(),
                    rs.getString("swear_filter_msg"),
                    mapper.readValue(
                            rs.getString("disabled_categories"),
                            mapper.typeFactory.constructCollectionType(List::class.java, String::class.java)
                    ),
                    rs.getInt("swear_filter_on") == 1
            )

            list[rs.getLong("id")] = guild
        }

        list
    }

    @Throws(NotFound::class)
    fun getGuild(id: Long): ButaGuild =
            guildCache[id] ?: throw NotFound("buta_guild")

    fun exists(id: Long): Boolean =
            guildCache.containsKey(id)

    /**
     * Types that can be modified.
     */
    private val validTypes = hashMapOf("prefix" to "", "swear_filter_msg" to "", "swear_filter_on" to 0, "join_role" to 0L)

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

        PostgreSql.getConnection("Update $id set $type")
                .prepareStatement("UPDATE buta.guilds SET $type = ? WHERE id = ?")
                .apply {
                    when (validTypes[type]!!) {
                        is String -> setString(1, value)
                        is Int -> setInt(1, value.toInt())
                        is Long -> setLong(1, value.toLong())
                        is Float -> setFloat(1, value.toFloat())
                        is Double -> setDouble(1, value.toDouble())
                    }

                    setLong(2, id)
                }
                .executeUpdate()

        refreshObj(id)
    }

    private suspend fun refreshObj(id: Long) {
        ButaInteraction.refreshGuild(id)

        val rs = PostgreSql.getConnection("Get ButaGuild $id")
                .prepareStatement("SELECT * FROM buta.guilds WHERE id = ?")
                .apply { setLong(1, id) }
                .executeQuery()

        val mapper = ObjectMapper()

        if (rs.next()) {
            val guild = ButaGuild(
                    rs.getLong("id").toString(),
                    rs.getString("prefix"),
                    rs.getString("join_message"),
                    rs.getString("leave_message"),
                    rs.getLong("join_role").toString(),
                    rs.getString("swear_filter_msg"),
                    mapper.readValue(
                            rs.getString("disabled_categories"),
                            mapper.typeFactory.constructCollectionType(List::class.java, String::class.java)
                    ),
                    rs.getInt("swear_filter_on") == 1
            )

            guildCache[rs.getLong("id")] = guild
        } else
            throw NotFound("buta_guild")
    }
}