package dev.shog.mojor.api

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.auth.getTokenFromCall
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import io.ktor.application.call
import io.ktor.response.respond
import io.ktor.routing.*

/**
 * Add the pages.
 */
fun Routing.tokenInteractionPages() {
    route("/token") {
        /**
         * Get a token's full data from their identifier.
         */
        get {
            call.respond(Response(call.isAuthorized()))
        }

        /**
         * Renew a token.
         */
        patch {
            call.isAuthorized(avoidExpire = true)
            call.respond(Response(TokenHandler.renewToken(call.getTokenFromCall())))
        }

        /**
         * Disable/Delete a token.
         */
        delete {
            call.respond(Response(TokenHandler.removeToken(call.isAuthorized())))
        }
    }
}