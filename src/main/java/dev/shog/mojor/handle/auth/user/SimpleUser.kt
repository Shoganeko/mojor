package dev.shog.mojor.handle.auth.user

/**
 * A simple user. This is a public representation of all users.
 *
 * @param username The name of the user.
 * @param id The ID of the user.
 * @param createdOn The date the user was created.
 */
data class SimpleUser(
        val username: String,
        val id: Long,
        val createdOn: Long
) {
    companion object {
        /**
         * Create a simple user from a [user].
         */
        fun fromUser(user: User): SimpleUser =
                SimpleUser(user.username, user.id, user.createdOn)
    }
}