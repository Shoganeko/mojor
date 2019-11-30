package dev.shog.mojor.auth.token.result

import dev.shog.mojor.auth.token.Token

/**
 * A token renewal result.
 *
 * @param token The token that was renewed.
 * @param successful If the renewal was successful
 * @param newExpire The new expire date.
 */
data class TokenRenewResult(val token: Token?, val successful: Boolean, val newExpire: Long)