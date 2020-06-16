package dev.shog.mojor.api.users

import dev.shog.lib.util.eitherOr
import dev.shog.mojor.api.notif.NotificationService
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.AlreadyLoggedInException
import dev.shog.mojor.handle.ArgumentDoesntMeet
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.InvalidAuthorization
import dev.shog.mojor.handle.auth.*
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import dev.shog.mojor.handle.auth.user.handle.UserLoginManager
import dev.shog.mojor.handle.auth.user.handle.UserManager
import dev.shog.mojor.handle.auth.user.result.UserLoginPayload
import dev.shog.mojor.handle.game.GameHandler
import io.ktor.application.call
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*

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
         * Change your own username.
         */
        post("/username") {
            val token = call.isAuthorized()
            val params = call.receiveParameters()

            if (!params.contains("username"))
                throw InvalidArguments("username")

            UserManager.changeUsername(token.owner, params["username"]!!)
            call.respond(Response())
        }

        /**
         * Change your own password.
         */
        post("/password") {
            val token = call.isAuthorized()
            val params = call.receiveParameters()

            if (!params.contains("password"))
                throw InvalidArguments("password")

            UserManager.changePassword(token.owner, params["password"]!!)
            call.respond(Response())
        }

        /**
         * Get a user's game record.
         */
        get("/games") {
            call.isAuthorized()

            val token = call.getTokenFromCall()
            val user = UserManager.getUser(token.owner)

            call.respond(GameHandler.getUserGameRecord(user.id).records)
        }

        /**
         * Add to a user's game record.
         */
        post("/games") {
            call.isAuthorized()

            val token = call.getTokenFromCall()
            val params = call.receiveParameters()

            val win = params["win"]?.toBoolean()?.eitherOr(1, 0)
            val map = params["map"]
            val score = params["score"]
            val game = params["game"]?.toInt()

            if (win == null || map == null || score == null || game == null)
                throw InvalidArguments("win", "map", "score", "game")

            GameHandler.uploadUserRecord(token.owner, game, win.toShort(), score, map)
            call.respond(HttpStatusCode.OK, Response())
        }

        route("/notifs") {
            /**
             * Get a user's notifications
             */
            get {
                val token = call.isAuthorized()

                call.respond(NotificationService.getNotificationsForUser(token.owner))
            }

            route("/{id}") {
                /**
                 * Delete a notification
                 */
                delete {
                    call.isAuthorized()

                    val token = call.getTokenFromCall()

                    NotificationService.closeNotification(call.parameters["id"].orEmpty(), token.owner)

                    call.respond(Response())
                }
            }
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
                        throw InvalidAuthorization()
                }
            }
        }
    }
}