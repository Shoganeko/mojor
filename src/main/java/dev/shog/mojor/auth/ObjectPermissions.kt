package dev.shog.mojor.auth

import dev.shog.mojor.auth.user.User
import org.json.JSONArray

/**
 * An object's permissions.
 *
 * @param permissions The permissions that the [ObjectPermissions] should have.
 *
 * @see Token
 * @see User
 */
class ObjectPermissions internal constructor(val permissions: ArrayList<Permissions>) {
    /**
     * [permissions] as a jsonArray
     */
    val jsonArray by lazy {
        val ar = JSONArray()

        permissions.forEach { perm ->
            ar.put(perm)
        }

        return@lazy ar
    }

    companion object {
        /**
         * Create an empty [ObjectPermissions].
         */
        fun empty() = ObjectPermissions(arrayListOf())

        /**
         * Create [ObjectPermissions] from a [JSONArray].
         */
        fun fromJsonArray(jsonArray: JSONArray): ArrayList<Any> = TODO()

        /**
         * Create [ObjectPermissions] from a [ArrayList]
         */
        fun fromArrayList(arrayList: ArrayList<Permissions>) = ObjectPermissions(arrayList)
    }
}