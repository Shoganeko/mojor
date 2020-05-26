package dev.shog.mojor.api.users

import dev.shog.lib.util.eitherOr
import dev.shog.mojor.api.notif.NotificationService
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.auth.*
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import dev.shog.mojor.handle.auth.user.handle.UserManager
import dev.shog.mojor.handle.auth.user.result.UserLoginPayload
import dev.shog.mojor.handle.game.GameHandler
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*

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
        val user = UserManager.getUser(token.owner)

        call.respond(Response(payload = user))
    }

    /**
     * Get a user's game record.
     */
    get("/v1/user/games") {
        call.isAuthorized()

        val token = call.getTokenFromCall()
        val user = UserManager.getUser(token.owner)

        call.respond(Response(payload = GameHandler.getUserGameRecord(user?.id!!).records))
    }

    /**
     * Add to a user's game record.
     */
    post("/v1/user/games") {
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
     * Remove from a user's game record.
     */
    delete("/v1/user/games") {
        call.isAuthorized()

        val token = call.getTokenFromCall()
        val params = call.receiveParameters()

        val date = params["date"]?.toLongOrNull()

        if (date == null) {
            call.respond(HttpStatusCode.BadRequest, Response("Date was not found or isn't an integer!"))
        } else {
            GameHandler.removeUserRecord(token.owner, date)
            call.respond(HttpStatusCode.OK, Response())
        }
    }

    /**
     * Get a user's notifications
     */
    get("/v1/user/notifs") {
        call.isAuthorized()

        val token = call.getTokenFromCall()

        call.respond(Response(payload = NotificationService.getNotificationsForUser(token.owner)))
    }

    /**
     * Delete a notification
     */
    delete("/v1/user/notifs/{id}") {
        call.isAuthorized()

        val token = call.getTokenFromCall()

        NotificationService.closeNotification(call.parameters["id"].orEmpty(), token.owner)

        call.respond(Response())
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
            val captchaResult = Captcha.verifyReCaptcha(captcha).join()

            if (captchaResult) {
                val user = UserManager.loginUsing(username, password, true)

                if (user != null) {
                    call.respond(HttpStatusCode.OK, Response(payload = UserLoginPayload(true, user, TokenHandler.createToken(user))))
                    return@post
                } else call.respond(HttpStatusCode.BadRequest, Response("Invalid username or password"))
            } else call.respond(HttpStatusCode.BadRequest, Response("Invalid reCAPTCHA"))
        } else {
            val user = UserManager.loginUsing(username, password, false)

            if (user == null)
                call.respond(HttpStatusCode.BadRequest, Response("Invalid Username or Password", UserLoginPayload()))
            else call.respond(HttpStatusCode.OK, Response(payload = UserLoginPayload(false, user, TokenHandler.createToken(user))))
        }
    }
}