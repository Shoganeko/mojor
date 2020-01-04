package dev.shog.mojor.auth.obj

/**
 * Permissions a token or user has.
 */
enum class Permissions {
    /**
     * The user has permission to manage Mojor.
     */
    APP_MANAGER,

    /**
     * The user has permission to manage Buta.
     */
    BUTA_MANAGER,

    /**
     * The user has permission to manage users.
     */
    USER_MANAGER,

    /**
     * The user has permission to manage MOTDs
     */
    MOTD_MANAGER;

    companion object {
        /**
         * Parse [perm] into [Permissions].
         */
        fun parse(perm: String): Permissions? {
            values().forEach { permi ->
                if (permi.toString().equals(perm, true))
                    return permi
            }

            return null
        }
    }
}