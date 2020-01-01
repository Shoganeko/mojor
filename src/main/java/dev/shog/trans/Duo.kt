package dev.shog.trans

class Duo<T, R>(val first: T? = null, val second: R? = null)

infix fun <T, R> T.duo(r: R): Duo<T, R> =
        Duo(this, r)