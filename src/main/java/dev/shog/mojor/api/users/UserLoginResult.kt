package dev.shog.mojor.api.users

import dev.shog.mojor.api.users.token.obj.Token
import dev.shog.mojor.api.users.obj.User

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