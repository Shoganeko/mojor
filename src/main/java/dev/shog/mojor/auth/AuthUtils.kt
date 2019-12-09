package dev.shog.mojor.auth

import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.auth.token.Token
import dev.shog.mojor.auth.token.TokenHolder
import dev.shog.mojor.auth.token.isExpired
import dev.shog.mojor.getMissing
import io.ktor.application.ApplicationCall
import io.ktor.auth.parseAuthorizationHeader
import reactor.core.publisher.Mono

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
        val tokenPerms = token.permissions.permissions

        if (!tokenPerms.containsAll(permissions.toList()))
            throw TokenMissingPermissions(arrayListOf(*getMissing(tokenPerms, permissions.toList()).toTypedArray()))
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
 * Check if a incoming connection is authorized and has [permissions].
 */
fun ApplicationCall.isAuthorized(mono: Mono<*>, vararg permissions: Permissions) {
    isAuthorized(*permissions)
            .also { mono.subscribe() }
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