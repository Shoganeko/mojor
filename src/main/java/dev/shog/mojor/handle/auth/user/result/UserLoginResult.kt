package dev.shog.mojor.handle.auth.user.result

import dev.shog.mojor.handle.auth.token.Token
import dev.shog.mojor.handle.auth.user.User

/**
 * A login payload.
 *
 * @param usingCaptcha If the user logged in using a reCAPTCHA
 * @param user The account that was used to log in
 * @param token The token created from the login.
 */
class UserLoginPayload(
        val usingCaptcha: Boolean = false,
        val user: User? = null,
        val token: Token? = null
)