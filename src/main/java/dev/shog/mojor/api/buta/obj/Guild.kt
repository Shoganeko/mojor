package dev.shog.mojor.api.buta.obj

/**
 * A guild object.
 */
class Guild : ButaObject {
    override var type: Int = 1
    override var id: Long = 0

    var prefix: String = "b!"
}