package dev.shog.mojor.handle.auth

import dev.shog.mojor.handle.auth.obj.Permissions
import dev.shog.mojor.handle.auth.token.Token
import dev.shog.mojor.handle.auth.token.TokenHolder
import dev.shog.mojor.handle.auth.token.isExpired
import dev.shog.mojor.getMissing
import io.ktor.application.ApplicationCall
import io.ktor.auth.parseAuthorizationHeader

/** Get a [Token] from an [ApplicationCall] */
fun ApplicationCall.getTokenFromCall(): Token {
    val header = getHeader(this) ?: throw AuthenticationException("no authentication")

    when (header.first.toLowerCase()) {
        "token" -> return TokenHolder.getToken(header.second)
                ?: throw AuthenticationException("invalid token")

        else -> throw InvalidAuthenticationType(header.first)
    }
}

/**
 * Check if a incoming connection is authorized and has [permissions].
 */
fun ApplicationCall.isAuthorized(vararg permissions: Permissions, avoidExpire: Boolean = false) {
    val token = getTokenFromCall()

    if (token.isExpired() && !avoidExpire)
        throw TokenExpiredException()

    if (permissions.isNotEmpty()) {
        val tokenPerms = token.permissions

        if (!tokenPerms.containsAll(permissions.toList()))
            throw TokenMissingPermissions(tokenPerms, getMissing(tokenPerms, permissions.toList()))
    }
}

/**
 * Check if an incoming request is authorized, and if it is return true.
 */
fun ApplicationCall.isAuthorizedBoolean(vararg permissions: Permissions): Boolean {
    return try {
        isAuthorized(*permissions)

        true
    } catch (ex: AuthenticationException) {
        false
    }
}

/**
 * Turn the [ApplicationCall]'s authorization header into a pair.
 * It is the type and token.
 */
internal fun getHeader(call: ApplicationCall): Pair<String, String>? {
    val header = call.request.parseAuthorizationHeader()
            ?.render()
            ?.split(" ")
            ?: return null

    return Pair(header[0], header[1])
}