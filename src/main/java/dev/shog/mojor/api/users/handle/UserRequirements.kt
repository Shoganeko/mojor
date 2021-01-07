package dev.shog.mojor.api.users.handle

object UserRequirements {
    //    private val PASSWORD_REGEX = Regex("^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@\$%^&*-]).{8,}\$") TODO
    private val USERNAME_REGEX = Regex("^[a-zA-Z0-9_-]+\$")

    /**
     * If [password] meets the password requirements.
     */
    fun passwordMeets(password: String): Boolean =
        128 >= password.length

    /**
     * If the [username] meets the username requirements.
     * This also checks if [username] already exists
     */
    fun usernameMeets(username: String): Boolean =
        (3..16).contains(username.length)
                && USERNAME_REGEX.matches(username)
                && !UserManager.nameExists(username)
}