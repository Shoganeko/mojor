package dev.shog.mojor.handle.auth.user.result

import dev.shog.mojor.handle.auth.token.obj.Token
import dev.shog.mojor.handle.auth.user.obj.User

/**
 * A login payload.
 *
 * @param user The account that was used to log in
 * @param token The token created from the login.
 */
class UserLoginPayload(
        val user: User? = null,
        val token: Token? = null
)