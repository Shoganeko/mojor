package dev.shog.mojor.auth.user.result

import dev.shog.mojor.auth.token.Token
import dev.shog.mojor.auth.user.User

/**
 * A user login result.
 *
 * @param user The user who logged in.
 * @param token THe created token from the login.
 * @param usingCaptcha If the user logged in using Captcha
 * @param error The error, if there's an error.
 */
data class UserLoginResult(
        val user: User? = null,
        val token: Token? = null,
        val usingCaptcha: Boolean = false,
        val error: String?
)