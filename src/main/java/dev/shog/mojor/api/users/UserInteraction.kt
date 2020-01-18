package dev.shog.mojor.api.users

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.auth.*
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.auth.user.UserManager
import dev.shog.mojor.auth.user.result.UserLoginPayload
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post

/**
 * Add the pages.
 */
fun Routing.userInteractionPages() {
    /**
     * Get a user's account information, excluding their password.
     */
    get("/v1/user") {
        call.isAuthorized()

        val token = call.getTokenFromCall()
        val user = UserHolder.getUser(token.owner)

        call.respond(Response(payload = user))
    }

    /**
     * Login to a user's account.
     */
    post("/v1/user") {
        if (call.isAuthorizedBoolean())
            throw AlreadyLoggedInException()

        val params = call.receiveParameters()

        val username = params["username"]
        val password = params["password"]

        if (username == null || password == null) {
            call.respond(HttpStatusCode.BadRequest, Response("Username or Password was not included", UserLoginPayload()))
            return@post
        }

        val captcha = params["captcha"]
        if (captcha != null) {
            val captchaResult = Captcha.verifyReCaptcha(captcha)

            if (captchaResult) {
                val user = UserManager.loginUsing(username, password, true)

                if (user != null) {
                    call.respond(HttpStatusCode.OK, Response(payload = UserLoginPayload(true, user, TokenManager.createToken(user))))
                    return@post
                }
            }

            call.respond(HttpStatusCode.BadRequest)
        } else {
            val user = UserManager.loginUsing(username, password, false)

            if (user == null)
                call.respond(HttpStatusCode.BadRequest, Response("Invalid Username or Password", UserLoginPayload()))
            else call.respond(HttpStatusCode.OK, Response(payload = UserLoginPayload(false, user, TokenManager.createToken(user))))
        }
    }
}