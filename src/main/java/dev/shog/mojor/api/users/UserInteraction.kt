package dev.shog.mojor.api.users

import dev.shog.mojor.auth.*
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.auth.user.UserManager
import dev.shog.mojor.auth.user.result.UserLoginResult
import dev.shog.mojor.execute
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import kotlinx.coroutines.launch
import reactor.core.publisher.Mono

/**
 * Add the pages.
 */
fun Routing.userInteractionPages() {
    /** Get a user's account information, excluding their password. */
    get("/v1/user") {
        call.isAuthorized()

        val token = call.getTokenFromCall()
        val user = UserHolder.getUser(token.owner)

        call.respond(user ?: Any())
    }

    /** Login to a user's account. */
    post("/v1/user") {
        if (call.isAuthorizedBoolean())
            throw AlreadyLoggedInException()

        val params = call.receiveParameters()

        val username = params["username"]
        val password = params["password"]

        if (username == null || password == null) {
            call.respond(HttpStatusCode.BadRequest, UserLoginResult(null, null, false, "Username or Password was not included."))
            return@post
        }

        val captcha = params["captcha"]
        if (captcha != null) {
            Captcha.verifyReCaptcha(captcha)
                    .filter { it }
                    .flatMap {
                        Mono.justOrEmpty(UserManager.loginUsing(username, password, true))
                                .flatMap { user ->
                                    TokenManager.createToken(user!!)
                                            .doOnNext { token -> launch { call.respond(UserLoginResult(user, token, usingCaptcha = true, error = null)) } }
                                }
                                .then()
                    }
                    .switchIfEmpty(execute(Thread {
                        launch {
                            call.respond(
                                    HttpStatusCode.BadRequest,
                                    UserLoginResult(null, null, usingCaptcha = false, error = "Invalid reCAPTCHA"))
                        }
                    }))
                    .subscribe()
        } else {
            val user = UserManager.loginUsing(username, password, false)

            if (user == null)
                call.respond(HttpStatusCode.BadRequest, UserLoginResult(null, null, usingCaptcha = false, error = "Invalid Username or Password"))
            else TokenManager.createToken(user)
                    .doOnNext { token -> launch { call.respond(UserLoginResult(user, token, usingCaptcha = false, error = null)) } }
                    .subscribe()
        }
    }
}