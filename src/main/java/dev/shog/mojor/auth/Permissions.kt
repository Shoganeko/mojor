package dev.shog.mojor.auth

/**
 * Permissions a token or user has.
 */
enum class Permissions {
    APP_MANAGER;

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