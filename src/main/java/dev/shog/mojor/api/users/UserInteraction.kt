package dev.shog.mojor.api.users

import dev.shog.mojor.auth.AlreadyLoggedInException
import dev.shog.mojor.auth.getTokenFromCall
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.isAuthorizedBoolean
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.auth.user.UserManager
import dev.shog.mojor.auth.user.result.UserLoginResult
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.coroutines.launch
import org.apache.commons.codec.digest.DigestUtils

/**
 * Add the pages.
 */
fun Routing.userInteractionPages() {
    /** Get a user's account information, excluding their password. */
    get("/v1/user") {
        call.isAuthorized()

        val token = call.getTokenFromCall()
        val user = UserHolder.getUser(token?.owner ?: -1)

        call.respond(user ?: Any())
    }

    /** Login to a user's account. */
    post("/v1/user") {
        if (call.isAuthorizedBoolean())
            throw AlreadyLoggedInException()

        val params = call.receiveParameters()

        val username = params["username"]
        var password = params["password"]

        if (username == null || password == null) {
            call.respond(HttpStatusCode.BadRequest, UserLoginResult(null, null, false))
            return@post
        }

        if (params["encr"]?.toBoolean() == true)
            password = DigestUtils.sha512Hex(password)

        val user = UserManager.loginUsing(username, password ?: "")

        if (user == null)
            call.respond(HttpStatusCode.BadRequest, UserLoginResult(null, null, false))
        else {
            TokenManager.createToken(user)
                    .doOnNext { token -> launch { call.respond(UserLoginResult(user, token, true)) } }
                    .subscribe()
        }
    }
}