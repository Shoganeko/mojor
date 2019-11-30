package dev.shog.mojor.api.buta

import dev.shog.mojor.api.buta.obj.ButaObject
import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Buta objects.
 */
object ButaObjectHandler {
    val OBJECTS = ConcurrentHashMap<Long, ButaObject>()

    /**Initialize [OBJECTS] by adding all from the database.*/
    fun init(): Mono<Void> =
            ButaDb.getAllObjects()
                    .doOnNext { obj -> OBJECTS[obj.id] = obj }
                    .collectList()
                    .then()

    /**
     * Initialize [OBJECTS] by adding fake objects to cut down on database interaction.
     */
    fun devInit(): Mono<Void> {
        val user = User()
        val guild = Guild()

        user.id = 1
        guild.id = 2

        return Flux.just(user, guild)
                .doOnNext { obj -> OBJECTS[obj.id] = obj }
                .then()
    }

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

        return ButaDb.updateObject(id, butaObject)
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

        return ButaDb.createObject(id, butaObject)
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

        return ButaDb.deleteObject(id, type)
    }

    /**
     * Get an object from the cache with/without checking database.
     *
     * @param id The ID of the object.
     * @param type The type of the object.
     * @param useDb If the function should use the database.
     */
    fun getObject(id: Long, type: Int, useDb: Boolean = false): Mono<ButaObject> =
            if (useDb) getObjectUsingDatabase(id, type) else getObjectFromCache(id)

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
                    .switchIfEmpty(ButaDb.getObject(id, type))
}