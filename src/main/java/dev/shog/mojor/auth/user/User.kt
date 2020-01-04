package dev.shog.mojor.auth.user

import dev.shog.mojor.auth.obj.ObjectPermissions

/**
 * A user.
 *
 * @param username A unique string that the user uses for logging in.
 * @param hashedPassword The sha-512 hashed password.
 * @param id A unique ID.
 * @param permissions The user's permissions.
 * @param createdOn The millis date that the user account was created on.
 */
data class User(
        val username: String,
        private val hashedPassword: String,
        val id: Long,
        val permissions: ObjectPermissions,
        val createdOn: Long,
        private var captchaVerified: Boolean = false
) {
    /** Checks if [password] equals [hashedPassword] */
    fun isCorrectPassword(password: String): Boolean =
            hashedPassword == password

    /** Get [hashedPassword]. */
    fun getPassword(): String =
            hashedPassword

    /** Set [captchaVerified] */
    fun setCaptcha(captcha: Boolean): User {
        captchaVerified = captcha

        return this
    }

    /** Check [captchaVerified] */
    fun isCaptchaVerified(): Boolean =
            captchaVerified
}