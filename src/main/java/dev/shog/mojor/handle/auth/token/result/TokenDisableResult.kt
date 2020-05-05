package dev.shog.mojor.handle.auth.token.result

import dev.shog.mojor.handle.auth.token.obj.Token

/**
 * A token disable result.
 *
 * @param token The token that was disabled.
 * @param successful If the renewal was successful
 */
data class TokenDisableResult(val token: Token?, val successful: Boolean)