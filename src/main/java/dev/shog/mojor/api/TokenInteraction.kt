package dev.shog.mojor.api

import dev.shog.mojor.auth.getTokenFromCall
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.token.disable
import dev.shog.mojor.auth.token.renew
import io.ktor.application.call
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
        call.respond(call.getTokenFromCall())
    }

    /** Renew a token. */
    patch("/v1/token") {
        call.isAuthorized(avoidExpire = true)
        val token = call.getTokenFromCall()

        token.renew()
                .doOnNext { result -> launch { call.respond(HttpStatusCode.OK, result) } }
                .subscribe()
    }

    /** Disable/Delete a token. */
    delete("/v1/token") {
        call.isAuthorized()
        val token = call.getTokenFromCall()

        token.disable()
                .doOnNext { result -> launch { call.respond(HttpStatusCode.OK, result) } }
                .subscribe()
    }
}