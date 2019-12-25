package dev.shog.mojor.api.buta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.shog.mojor.api.buta.obj.ButaObject
import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import dev.shog.mojor.handle.db.PostgreSql
import reactor.core.publisher.Flux
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
    fun updateObject(id: Long, butaObject: ButaObject): Mono<Boolean> =
            Mono.justOrEmpty(PostgreSql.createConnection())
                    .switchIfEmpty(Mono.error(NullPointerException("PostgreSql was null!")))
                    .map { sql -> sql!!.prepareStatement("UPDATE buta.objs SET 'js'=? WHERE 'id'=? ") }
                    .doOnNext { sql -> sql.setString(1, ObjectMapper().writeValueAsString(butaObject)) }
                    .doOnNext { sql -> sql.setLong(2, id) }
                    .map { sql -> sql.execute() }

    /**
     * Create an object in the database.
     *
     * @param id The ID of the new object.
     * @param butaObject The new object.
     */
    fun createObject(id: Long, butaObject: ButaObject): Mono<Boolean> =
            PostgreSql.monoConnection()
                    .map { sql -> sql!!.prepareStatement("INSERT INTO buta.objs(id, js, type) VALUES (?, ?, ?)") }
                    .doOnNext { sql -> sql.setLong(1, id) }
                    .doOnNext { sql -> sql.setString(2, ObjectMapper().writeValueAsString(butaObject)) }
                    .doOnNext { sql -> sql.setInt(3, butaObject.type) }
                    .map { sql -> sql.execute() }

    /**
     * Delete an object in the database.
     *
     * @param id The ID of the object.
     * @param type The type of object.
     */
    fun deleteObject(id: Long, type: Int): Mono<Boolean> =
            Mono.justOrEmpty(PostgreSql.createConnection())
                    .switchIfEmpty(Mono.error(NullPointerException("PostgreSql was null!")))
                    .map { sql -> sql!!.prepareStatement("DELETE FROM buta.objs WHERE id=?") }
                    .doOnNext { sql -> sql.setLong(1, id) }
                    .map { sql -> sql.execute() }

    /**
     * Get an object in the database.
     *
     * @param id The ID of the object.
     * @param type The type of object.
     */
    fun getObject(id: Long, type: Int): Mono<ButaObject> =
            Mono.justOrEmpty(PostgreSql.createConnection())
                    .switchIfEmpty(Mono.error(NullPointerException("PostgreSql was null!")))
                    .map { sql -> sql!!.prepareStatement("SELECT * FROM buta.objs WHERE id = ?") }
                    .doOnNext { sql -> sql.setLong(1, id) }
                    .map { sql -> sql.executeQuery() }
                    .doOnNext { rs -> rs.next() }
                    .map { rs ->
                        when (type) {
                            1 -> ObjectMapper().readValue<Guild>(rs.getString("js"))
                            2 -> ObjectMapper().readValue<User>(rs.getString("js"))
                            else -> throw Exception("Invalid Type!")
                        }
                    }

    /**
     * Get all objects in the database.
     */
    fun getAllObjects(): Flux<ButaObject> =
            PostgreSql.monoConnection()
                    .map { sql -> sql!!.prepareStatement("SELECT * FROM buta.objs") }
                    .map { sql -> sql.executeQuery() }
                    .map { rs ->
                        val list = arrayListOf<Pair<String, Int>>()

                        while (rs.next()) {
                            list.add(Pair(rs.getString("js"), rs.getInt("type")))
                        }

                        list
                    }
                    .flatMapMany { list -> Flux.fromIterable(list) }
                    .map { data ->
                        when (data.second) {
                            1 -> ObjectMapper().readValue<Guild>(data.first)
                            2 -> ObjectMapper().readValue<User>(data.first)
                            else -> throw Exception("Invalid Type!")
                        }
                    }
}