package dev.shog.mojor.handle

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.auth.obj.Permission
import io.ktor.application.call
import io.ktor.features.StatusPages
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond

class TokenExpiredException : Exception()

class TokenMissingPermissions(val tokenPerms: Collection<Permission>, val requiredPerms: Collection<Permission>) : Exception()

class InvalidAuthorization(val reason: String) : Exception()

class AlreadyLoggedInException : Exception()

class InvalidArguments(vararg val args: String) : Exception()

class InvalidCaptcha : Exception()

class ArgumentDoesntMeet(val arg: String) : Exception()

class AlreadyExists(val type: String) : Exception()

class NotFound(val type: String) : Exception()

/**
 * Register [StatusPages] for exceptions.
 */
fun StatusPages.Configuration.registerExceptions() {
    exception<NotFound> {
        call.respond(HttpStatusCode.BadRequest, Response(response = "That ${it.type} could not be found!"))
    }

    exception<AlreadyExists> {
        call.respond(HttpStatusCode.BadRequest, Response(response = "That ${it.type} already exists!"))
    }

    exception<TokenExpiredException> { ex ->
        call.respond(HttpStatusCode.BadRequest, Response(response = "That token is expired!"))
    }

    exception<ArgumentDoesntMeet> {
        call.respond(HttpStatusCode.BadRequest, Response(response = "${it.arg} doesn't meet the requirements!"))
    }

    exception<TokenMissingPermissions> { ex ->
        val missingPerms = ex.tokenPerms.filterNot { ex.requiredPerms.contains(it) }

        call.respond(HttpStatusCode.BadRequest, Response(response = "Token is missing permissions: ${missingPerms.joinToString()}"))
    }

    exception<InvalidArguments> {
        call.respond(HttpStatusCode.BadRequest, Response(response = "Invalid arguments, requires ${it.args.joinToString()}"))
    }

    exception<AlreadyLoggedInException> {
        call.respond(HttpStatusCode.BadRequest, Response(response = "You are already logged in!"))
    }

    exception<InvalidAuthorization> {
        call.respond(HttpStatusCode.Unauthorized, Response(response = "Invalid authorization, reason: ${it.reason}"))
    }

    exception<InvalidCaptcha> {
        call.respond(HttpStatusCode.BadRequest, Response(response = "Invalid reCAPTCHA"))
    }
}