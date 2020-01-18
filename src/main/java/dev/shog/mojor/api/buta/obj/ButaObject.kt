package dev.shog.mojor.api.buta.obj

import java.io.Serializable

/**
 * A buta object.
 */
interface ButaObject : Serializable {
    /** The ID of the Buta object. */
    var id: Long

    /** The type of the Buta object. */
    var type: Int
}