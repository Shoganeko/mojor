package dev.shog.mojor.api.users

import dev.shog.mojor.auth.UserLoginRequest
import dev.shog.mojor.auth.getTokenFromCall
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.auth.user.UserManager
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.coroutines.launch

/**
 * Add the pages.
 */
fun Routing.userInteractionPages() {
    get("/v1/user") {
        call.isAuthorized()

        val token = call.getTokenFromCall()
        val user = UserHolder.getUser(token?.owner ?: -1)

        call.respond(user ?: Any())
    }

    post("/v1/user") {
        val params = call.receiveParameters()

        val username = params["username"]
        val password = params["password"]

        if (username == null || password == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = UserManager.loginUsing(username, password)

        if (user == null)
            call.respond(HttpStatusCode.BadRequest)
        else {
            TokenManager.createToken(user)
                    .doOnNext { token ->
                        launch {
                            call.respond(UserLoginRequest(user, token))
                        }
                    }
                    .subscribe()
        }
    }
}