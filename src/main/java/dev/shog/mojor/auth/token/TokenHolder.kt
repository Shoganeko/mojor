package dev.shog.mojor.auth.token

import java.util.concurrent.ConcurrentHashMap

/**
 * Holds all existing tokens.
 */
object TokenHolder {
    internal val TOKENS = ConcurrentHashMap<String, Token>()
}