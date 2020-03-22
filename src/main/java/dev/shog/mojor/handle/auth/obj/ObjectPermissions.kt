package dev.shog.mojor.handle.auth.obj

import dev.shog.mojor.handle.auth.token.Token
import dev.shog.mojor.handle.auth.user.User
import org.json.JSONArray

/**
 * An object's permissions.
 *
 * @see Token
 * @see User
 */
class ObjectPermissions internal constructor() : ArrayList<Permissions>() {
    companion object {
        /**
         * Create an empty [ObjectPermissions].
         */
        fun empty() = ObjectPermissions()

        /**
         * Create [ObjectPermissions] from a [JSONArray].
         */
        fun fromJsonArray(jsonArray: JSONArray): ObjectPermissions {
            val ar = ArrayList<Permissions>()

            if (jsonArray.length() == 0)
                return empty()

            (0 until jsonArray.length())
                    .asSequence()
                    .map { jsonArray[it].toString() }
                    .mapNotNullTo(ar) { Permissions.parse(it) }

            return ObjectPermissions().apply {
                addAll(ar)
            }
        }

        /**
         * Create [ObjectPermissions] from a [ArrayList]
         */
        fun fromArrayList(arrayList: ArrayList<Permissions>) =
                ObjectPermissions().apply { addAll(arrayList) }
    }
}