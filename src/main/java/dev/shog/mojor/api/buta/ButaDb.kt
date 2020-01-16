package dev.shog.mojor.api.buta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.shog.mojor.api.buta.obj.ButaObject
import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import dev.shog.mojor.handle.db.PostgreSql
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import reactor.core.publisher.Mono

/**
 * Database interaction for Buta.
 */
object ButaDb {
    /**
     * Update and object in the database.
     *
     * @param id The ID of the object.
     * @param butaObject The object.
     */
    suspend fun updateObject(id: Long, butaObject: ButaObject): Boolean = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("UPDATE buta.objs SET js=? WHERE id=?")

        pre.setString(1, ObjectMapper().writeValueAsString(butaObject))
        pre.setLong(2, id)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.execute() }
    }

    /**
     * Create an object in the database.
     *
     * @param id The ID of the new object.
     * @param butaObject The new object.
     */
    suspend fun createObject(id: Long, butaObject: ButaObject): Boolean = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("INSERT INTO buta.objs(id, js, type) VALUES (?, ?, ?)")

        pre.setLong(1, id)
        pre.setString(2, ObjectMapper().writeValueAsString(butaObject))
        pre.setInt(3, butaObject.type)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.execute() }
    }

    /**
     * Delete an object in the database.
     *
     * @param id The ID of the object.
     * @param type The type of object.
     */
    suspend fun deleteObject(id: Long, type: Int): Boolean = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("DELETE FROM buta.objs WHERE id=?")

        pre.setLong(1, id)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.execute() }
    }

    /**
     * Get an object in the database.
     *
     * @param id The ID of the object.
     * @param type The type of object.
     */
    suspend fun getObject(id: Long, type: Int): ButaObject = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("SELECT * FROM buta.objs WHERE id = ?")

        pre.setLong(1, id)

        val rs = withContext(Dispatchers.Unconfined) { pre.executeQuery() }

        while (rs.next()) {
            return@coroutineScope when (type) {
                1 -> ObjectMapper().readValue<Guild>(rs.getString("js"))
                2 -> ObjectMapper().readValue<User>(rs.getString("js"))
                else -> throw Exception("Invalid Type!")
            }
        }

        throw Exception("Invalid ID/Type")
    }

    /**
     * Get all objects in the database.
     */
    suspend fun getAllObjects(): ArrayList<ButaObject> = coroutineScope {
        val rs = withContext(Dispatchers.Unconfined) {
            PostgreSql.createConnection()
                    .prepareStatement("SELECT  * FROM buta.objs")
                    .executeQuery()
        }

        val array = arrayListOf<ButaObject>()

        while (rs.next()) {
            array.add(when (rs.getInt("int")) {
                1 -> ObjectMapper().readValue<Guild>(rs.getString("js"))
                2 -> ObjectMapper().readValue<User>(rs.getString("js"))
                else -> throw Exception("Invalid Type!")
            })
        }

        return@coroutineScope array
    }
}