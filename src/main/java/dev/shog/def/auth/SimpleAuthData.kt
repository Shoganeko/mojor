package dev.shog.def.auth

import dev.shog.def.auth.objs.IAuthData

/**
 * Used for the [SimpleAuthDevice].
 */
data class SimpleAuthData(val username: String, val password: String) : IAuthData