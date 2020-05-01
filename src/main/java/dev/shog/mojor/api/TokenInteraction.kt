package dev.shog.mojor.api

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.auth.getTokenFromCall
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.patch

/**
 * Add the pages.
 */
fun Routing.tokenInteractionPages() {
    /** Get a token's full data from their identifier. */
    get("/v1/token") {
        call.isAuthorized()
        call.respond(Response(payload = call.getTokenFromCall()))
    }

    /** Renew a token. */
    patch("/v1/token") {
        call.isAuthorized(avoidExpire = true)
        call.respond(Response(payload = TokenHandler.renewToken(call.getTokenFromCall())))
    }

    /** Disable/Delete a token. */
    delete("/v1/token") {
        call.isAuthorized()
        call.respond(Response(payload = TokenHandler.removeToken(call.getTokenFromCall())))
    }
}