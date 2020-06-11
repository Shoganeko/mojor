package dev.shog.mojor.api.users

import dev.shog.lib.util.eitherOr
import dev.shog.mojor.api.notif.NotificationService
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.AlreadyLoggedInException
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.InvalidAuthorization
import dev.shog.mojor.handle.auth.*
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
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
            call.isAuthorized()

            val token = call.getTokenFromCall()
            val user = UserManager.getUser(token.owner)

            call.respond(Response(payload = user))
        }

        /**
         * Get a user's game record.
         */
        get("/games") {
            call.isAuthorized()

            val token = call.getTokenFromCall()
            val user = UserManager.getUser(token.owner)

            call.respond(Response(payload = GameHandler.getUserGameRecord(user?.id!!).records))
        }

        /**
         * Add to a user's game record.
         */
        post("/games") {
            call.isAuthorized()

            val token = call.getTokenFromCall()
            val params = call.receiveParameters()

            if (
                    !params.contains("map")
                    || !params.contains("win")
                    || !params.contains("score")
                    || !params.contains("game")
            ) {
                call.respond(HttpStatusCode.BadRequest, Response("Map, Win or Score was not found."))
            } else {
                val win = params["win"]?.toBoolean()?.eitherOr(1, 0)!!
                val map = params["map"]!!
                val score = params["score"]!!
                val game = params["game"]!!.toInt()

                GameHandler.uploadUserRecord(token.owner, game, win.toShort(), score, map)
                call.respond(HttpStatusCode.OK, Response())
            }
        }

        /**
         * Get a user's notifications
         */
        get("/notifs") {
            call.isAuthorized()

            val token = call.getTokenFromCall()

            call.respond(Response(payload = NotificationService.getNotificationsForUser(token.owner)))
        }

        /**
         * Delete a notification
         */
        delete("/notifs/{id}") {
            call.isAuthorized()

            val token = call.getTokenFromCall()

            NotificationService.closeNotification(call.parameters["id"].orEmpty(), token.owner)

            call.respond(Response())
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
                        call.respond(Response(payload = UserLoginPayload(user, TokenHandler.createToken(user))))
                    } else
                        throw InvalidAuthorization()
                }
            }
        }
    }
}