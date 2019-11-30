package dev.shog.mojor.auth.obj

/**
 * Permissions a token or user has.
 */
enum class Permissions {
    APP_MANAGER, BUTA_MANAGER, USER_MANAGER, MOTD_MANAGER;

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