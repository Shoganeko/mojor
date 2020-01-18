package dev.shog.mojor.api.buta

import dev.shog.mojor.api.buta.obj.ButaObject
import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages Buta objects.
 */
object ButaObjectHandler {
    val OBJECTS = ConcurrentHashMap<Long, ButaObject>()

    /**Initialize [OBJECTS] by adding all from the database.*/
    suspend fun init() {
        ButaDb.getAllObjects().forEach { obj -> OBJECTS[obj.id] = obj }
    }

    /**
     * Initialize [OBJECTS] by adding fake objects to cut down on database interaction.
     */
    fun devInit() {
        OBJECTS.apply {
            put(0, Guild().apply { id = 0 })
            put(1, User().apply { id = 1 })
        }
    }

    /**
     * Update an object.
     *
     * @param id The ID of the object.
     * @param butaObject The new object.
     */
    suspend fun updateObject(id: Long, butaObject: ButaObject) {
        if (!OBJECTS.containsKey(id))
            throw Exception("Object doesn't exist!")
        else OBJECTS[id] = butaObject

        return ButaDb.updateObject(id, butaObject)
    }

    /**
     * Create an object.
     *
     * @param id The ID of the new object.
     * @param butaObject The new object.
     */
    suspend fun createObject(id: Long, butaObject: ButaObject) {
        if (OBJECTS.containsKey(id))
            throw Exception("This object already exists!")
        else OBJECTS[id] = butaObject

        return ButaDb.createObject(id, butaObject)
    }


    /**
     * Delete an object. If it doesn't exist in [OBJECTS] then it won't attempt removing it from the database.
     *
     * @param id The ID of the object.
     * @param type The type of the object.
     */
    suspend fun deleteObject(id: Long, type: Int) {
        OBJECTS.remove(id)
                ?: throw Exception()

        return ButaDb.deleteObject(id, type)
    }

    /**
     * Get an object from the cache with/without checking database.
     *
     * @param id The ID of the object.
     * @param type The type of the object.
     * @param useDb If the function should use the database.
     */
    suspend fun getObject(id: Long, type: Int, useDb: Boolean = false): ButaObject? =
            if (useDb) getObjectUsingDatabase(id, type) else getObjectFromCache(id)

    /**
     * Get an object from [OBJECTS].
     *
     * @param id The ID of the object.
     */
    private fun getObjectFromCache(id: Long): ButaObject? =
            OBJECTS[id]

    /**
     * Get an object from the cache, then try the database.
     *
     * @param id The ID of the object.
     * @param type The type of the object.
     */
    private suspend fun getObjectUsingDatabase(id: Long, type: Int): ButaObject =
            getObjectFromCache(id)
                    ?: ButaDb.getObject(id, type)
}