package dev.shog.trans

/**
 * A pair, but it's able to be used through Jackson for transport through Mojor.
 */
class Duo<T, R>(val first: T? = null, val second: R? = null)

/**
 * Similar to function [to].
 */
infix fun <T, R> T.duo(r: R): Duo<T, R> =
        Duo(this, r)