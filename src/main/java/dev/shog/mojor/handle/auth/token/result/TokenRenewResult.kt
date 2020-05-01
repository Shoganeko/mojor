package dev.shog.mojor.handle.auth.token.result

import dev.shog.mojor.handle.auth.token.obj.Token

/**
 * A token renewal result.
 *
 * @param token The token that was renewed.
 * @param successful If the renewal was successful
 * @param newExpire The new expire date.
 */
data class TokenRenewResult(val token: Token?, val successful: Boolean, val newExpire: Long)