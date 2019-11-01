package dev.shog.mojor.auth

import dev.shog.mojor.auth.token.Token
import java.util.concurrent.ConcurrentHashMap

object AuthHandler {
    val TOKENS = ConcurrentHashMap<String, Token>()

    fun getTokenByString(token: String): Token? = TOKENS[token]
}