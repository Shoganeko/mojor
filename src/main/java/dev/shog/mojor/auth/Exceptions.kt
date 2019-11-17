package dev.shog.mojor.auth

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
class TokenMissingPermissions :
        AuthenticationException("token missing permissions")

/**
 * An invalid token type was inputted
 */
class InvalidAuthenticationType :
        AuthenticationException("invalid authentication type (use token)")