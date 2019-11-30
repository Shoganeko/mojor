package dev.shog.mojor.api

import dev.shog.mojor.auth.AuthenticationException
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.isAuthorizedAvoidExpired
import dev.shog.mojor.auth.token.TokenHolder
import dev.shog.mojor.auth.token.disable
import dev.shog.mojor.auth.token.renew
import io.ktor.application.call
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.patch
import kotlinx.coroutines.launch

/**
 * Add the pages.
 */
fun Routing.tokenInteractionPages() {
    /** Get a token's full data from their identifier. */
    get("/v1/token") {
        call.isAuthorized()
        val token = call.request.parseAuthorizationHeader()?.render()?.split(" ")?.get(1) ?: ""
        val parsedToken = TokenHolder.getToken(token) ?: throw AuthenticationException("invalid token")

        call.respond(parsedToken)
    }

    /** Renew a token. */
    patch("/v1/token") {
        call.isAuthorizedAvoidExpired()
        val token = call.request.parseAuthorizationHeader()?.render()?.split(" ")?.get(1) ?: ""
        val parsedToken = TokenHolder.getToken(token) ?: throw AuthenticationException("invalid token")

        parsedToken.renew()
                .doOnNext { result -> launch { call.respond(HttpStatusCode.OK, result) } }
                .subscribe()
    }

    /** Disable/Delete a token. */
    delete("/v1/token") {
        call.isAuthorized()
        val token = call.request.parseAuthorizationHeader()?.render()?.split(" ")?.get(1) ?: ""
        val parsedToken = TokenHolder.getToken(token) ?: throw AuthenticationException("invalid token")

        parsedToken.disable()
                .doOnNext { result -> launch { call.respond(HttpStatusCode.OK, result) } }
                .subscribe()
    }
}