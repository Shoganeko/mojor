package dev.shog.mojor.handle.auth

import dev.shog.mojor.handle.auth.obj.Permission

/**
 * An authentication exception
 */
class TokenExpiredException :
        AuthenticationException("expired token")

/**
 * An authentication exception
 */
open class AuthenticationException(reason: String) :
        Exception("User is not authorized due to $reason")

/**
 * A token is missing permissions
 */
class TokenMissingPermissions(tokenPerms: Collection<Permission>, missingPerms: Collection<Permission>) :
        AuthenticationException("token missing permissions. (HAS: $tokenPerms, MISSING: $missingPerms)")

/**
 * An invalid token type was inputted
 */
class InvalidAuthenticationType(type: String) :
        AuthenticationException("$type is an invalid authentication type (use token)")

/**
 * User is already authorized
 */
class AlreadyLoggedInException :
        Exception("Token cannot be created due to user already being authorized in")