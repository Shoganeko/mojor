package dev.shog.mojor.api.users

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.auth.*
import dev.shog.mojor.api.users.token.handle.TokenHandler
import dev.shog.mojor.api.users.handle.UserLoginManager
import dev.shog.mojor.api.users.handle.UserManager
import dev.shog.mojor.api.users.handle.UserRequirements
import dev.shog.mojor.api.users.obj.User
import dev.shog.mojor.handle.*
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*
import org.mindrot.jbcrypt.BCrypt

/**
 * Add the pages.
 */
fun Routing.userInteractionPages() {
    route("/user") {
        /**
         * Get a user's account information, excluding their password.
         */
        get {
            val token = call.isAuthorized()
            val user = UserManager.getUser(token.owner)

            call.respond(user)
        }

        /**
         * Change [param].
         */
        suspend fun ApplicationCall.changeParam(param: String): Pair<User, String> {
            val token = isAuthorized()
            val params = receiveParameters()

            val parm = params[param]
                ?: throw InvalidArguments(param)

            return UserManager.getUser(token.owner) to parm
        }

        /**
         * Change your own username.
         */
        post("/username") {
            val (user, param) = call.changeParam("username")

            if (!UserRequirements.usernameMeets(param))
                throw InvalidArguments("username")

            user.username = param

            call.respond(Response())
        }

        /**
         * Change your own password.
         */
        post("/password") {
            val (user, password) = call.changeParam("password")

            if (!UserRequirements.passwordMeets(password))
                throw InvalidArguments("password]")

            user.password = BCrypt.hashpw(password, BCrypt.gensalt())

            call.respond(Response())
        }

        /**
         * Get a user's login attempts
         */
        get("/attempts") {
            val user = call.isAuthorized()
            val params = call.request.queryParameters

            if (params.contains("limit")) {
                val limit = params["limit"]?.toIntOrNull()
                    ?: throw InvalidArguments("limit")

                if (limit > 500)
                    throw ArgumentDoesntMeet("limit")

                call.respond(UserLoginManager.getLoginAttempts(user.owner, limit))
            }

            call.respond(UserLoginManager.getLoginAttempts(user.owner))
        }

        /**
         * Login to a user's account.
         */
        post {
            if (call.isAuthorizedBoolean())
                throw AlreadyLoggedInException()

            val params = call.receiveParameters()

            val username = params["username"]
            val password = params["password"]
            val captcha = params["captcha"]

            when {
                username == null || password == null || captcha == null ->
                    throw InvalidArguments("username", "password", "captcha")

                !Captcha.verifyReCaptcha(captcha) ->
                    call.respond(HttpStatusCode.BadRequest, Response("Invalid reCAPTCHA"))

                else -> {
                    val user = UserManager.loginUsing(username, password, call.request.origin.remoteHost)

                    if (user != null) {
                        call.respond(UserLoginPayload(user, TokenHandler.createToken(user)))
                    } else
                        throw InvalidAuthorization("invalid username or password")
                }
            }
        }
    }
}