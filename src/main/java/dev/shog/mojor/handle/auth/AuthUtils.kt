package dev.shog.mojor.handle.auth

import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.token.obj.Token
import dev.shog.mojor.handle.InvalidAuthorization
import dev.shog.mojor.handle.TokenExpiredException
import dev.shog.mojor.handle.TokenMissingPermissions
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import io.ktor.application.ApplicationCall
import io.ktor.auth.parseAuthorizationHeader

/** Get a [Token] from an [ApplicationCall] */
fun ApplicationCall.getTokenFromCall(): Token {
    val header = getHeader(this)

    when (header.first.toLowerCase()) {
        "bearer" -> return TokenHandler.getCachedToken(header.second)
                ?: throw InvalidAuthorization()
    }

    throw InvalidAuthorization()
}

/**
 * Check if a incoming connection is authorized and has [permissions].
 */
fun ApplicationCall.isAuthorized(vararg permissions: Permission, avoidExpire: Boolean = false): Token {
    val token = getTokenFromCall()

    if (TokenHandler.isTokenExpired(token) && !avoidExpire)
        throw TokenExpiredException()

    if (permissions.isNotEmpty()) {
        val tokenPerms = token.permissions

        if (permissions.any { perm -> !tokenPerms.contains(perm) })
            throw TokenMissingPermissions(tokenPerms, permissions.toList())
    }

    return token
}

/**
 * Check if an incoming request is authorized, and if it is return true.
 */
fun ApplicationCall.isAuthorizedBoolean(vararg permissions: Permission): Boolean {
    return try {
        isAuthorized(*permissions)

        true
    } catch (ex: InvalidAuthorization) {
        false
    }
}

/**
 * Turn the [ApplicationCall]'s authorization header into a pair.
 * It is the type and token.
 */
internal fun getHeader(call: ApplicationCall): Pair<String, String> {
    val header = call.request.parseAuthorizationHeader()
            ?.render()
            ?.split(" ")
            ?: throw InvalidAuthorization()

    return Pair(header[0], header[1])
}