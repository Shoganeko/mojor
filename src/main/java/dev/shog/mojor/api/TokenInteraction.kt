package dev.shog.mojor.api

import dev.shog.mojor.auth.isAuthorized
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

/**
 * Add the pages.
 */
fun Routing.tokenInteractionPages() {
    get("/v1/token") {
        call.isAuthorized()
        val token = call.request.parseAuthorizationHeader()?.render()?.split(" ")?.get(1) ?: ""
        val parsedToken = TokenHolder.getToken(token)

        if (parsedToken == null)
            call.respond(HttpStatusCode.BadRequest)
        else call.respond(parsedToken)
    }

    patch("/v1/token") {
        call.isAuthorized()
        val token = call.request.parseAuthorizationHeader()?.render()?.split(" ")?.get(1) ?: ""
        val parsedToken = TokenHolder.getToken(token)

        if (parsedToken == null)
            call.respond(HttpStatusCode.BadRequest)
        else {
            parsedToken.renew()
            call.respond(HttpStatusCode.OK)
        }
    }

    delete("/v1/token") {
        call.isAuthorized()
        val token = call.request.parseAuthorizationHeader()?.render()?.split(" ")?.get(1) ?: ""
        val parsedToken = TokenHolder.getToken(token)

        if (parsedToken == null)
            call.respond(HttpStatusCode.BadRequest)
        else {
            parsedToken.disable()
            call.respond(HttpStatusCode.OK)
        }
    }
}