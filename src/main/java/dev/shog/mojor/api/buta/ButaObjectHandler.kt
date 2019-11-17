package dev.shog.mojor.api.buta

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import dev.shog.mojor.api.buta.obj.ButaObject
import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import dev.shog.mojor.db.PostgreSql
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

object ButaObjectHandler {
    private val OBJECTS = ConcurrentHashMap<Long, ButaObject>()

    /**
     * Initialize [OBJECTS] by adding all from the database.
     */
    fun init(): Mono<Void> =
            Database.getAllObjects()
                    .doOnNext { obj -> OBJECTS[obj.id] = obj }
                    .collectList()
                    .then()

    /**
     * Initialize [OBJECTS] by adding fake objects to cut down on database interaction.
     */
    fun devInit(): Mono<Void> =
            Flux.just(Guild(), User())
                    .doOnNext { obj -> OBJECTS[obj.id] = obj }
                    .collectList()
                    .then()

    /**
     * Update an object.
     *
     * @param id The ID of the object.
     * @param butaObject The new object.
     */
    fun updateObject(id: Long, butaObject: ButaObject): Mono<Boolean> {
        if (!OBJECTS.containsKey(id))
            return Mono.error(NullPointerException("Object doesn't exist!"))
        else OBJECTS[id] = butaObject

        return Database.updateObject(id, butaObject)
    }

    /**
     * Create an object.
     *
     * @param id The ID of the new object.
     * @param butaObject The new object.
     */
    fun createObject(id: Long, butaObject: ButaObject): Mono<Boolean> {
        if (OBJECTS.containsKey(id))
            return Mono.error(NullPointerException("This object already exists!"))
        else OBJECTS[id] = butaObject

        return Database.createObject(id, butaObject)
    }


    /**
     * Delete an object. If it doesn't exist in [OBJECTS] then it won't attempt removing it from the database.
     *
     * @param id The ID of the object.
     * @param type The type of the object.
     */
    fun deleteObject(id: Long, type: Int): Mono<Boolean> {
        try {
            OBJECTS.remove(id) ?: return Mono.error(NullPointerException())
        } catch (e: Exception) {
            return Mono.error(e)
        }

        return Database.deleteObject(id, type)
    }

    /**
     * Get an object from the cache with/without checking database.
     *
     * @param id The ID of the object.
     * @param type The type of the object.
     * @param useDb If the function should use the database.
     */
    fun getObject(id: Long, type: Int, useDb: Boolean = false): Mono<ButaObject> =
            if (useDb)
                getObjectUsingDatabase(id, type)
            else getObjectFromCache(id)

    /**
     * Get an object from [OBJECTS].
     *
     * @param id The ID of the object.
     */
    private fun getObjectFromCache(id: Long): Mono<ButaObject> =
            Mono.justOrEmpty(OBJECTS[id])

    /**
     * Get an object from the cache, then try the database.
     *
     * @param id The ID of the object.
     * @param type The type of the object.
     */
    private fun getObjectUsingDatabase(id: Long, type: Int): Mono<ButaObject> =
            getObjectFromCache(id)
                    .switchIfEmpty(Database.getObject(id, type))

    /**
     * Database interaction for Buta.
     */
    object Database {
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
                Mono.justOrEmpty(PostgreSql.createConnection())
                        .switchIfEmpty(Mono.error(NullPointerException("PostgreSql was null!")))
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
}