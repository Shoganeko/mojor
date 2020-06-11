package dev.shog.mojor.api.users

import dev.shog.mojor.api.notif.NotificationService
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.user.handle.UserManager
import dev.shog.mojor.handle.auth.user.obj.User
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*

/**
 * Add the pages.
 */
fun Routing.globalUserInteractionPages() {
    /** Get all users. */
    get("/users") {
        call.isAuthorized(Permission.USER_MANAGER)
        call.respond(Response(payload = UserManager.getUsers()))
    }

    route("/user") {
        route("/{id}") {
            /**
             * Update a selected user with new user data.
             */
            patch("/name") {
                TODO()
            }

            /**
             * Delete a selected user.
             */
            delete("/user/{id}") {
                call.isAuthorized(Permission.USER_MANAGER)

                val id = call.parameters["id"]
                val user = UserManager.getUser(getUuid(id))

                if (user != null) {
                    UserManager.deleteUser(user.id)
                    call.respond(Response())
                } else throw NotFound("user")
            }

            /**
             * Get a selected user.
             */
            get("/user/{id}") {
                call.isAuthorized(Permission.USER_MANAGER)
                val id = call.parameters["id"]
                val user = UserManager.getUser(getUuid(id))

                if (user == null)
                    call.respond(HttpStatusCode.BadRequest, Response("Invalid User"))
                else call.respond(Response(payload = user))
            }

            /**
             * Give a user a notification
             */
            put("/user/{id}/notif") {
                call.isAuthorized(Permission.USER_MANAGER)

                val id = call.parameters["id"]
                val content = call.receiveParameters()["content"]
                val user = UserManager.getUser(getUuid(id))

                if (user == null)
                    call.respond(HttpStatusCode.BadRequest, Response("Invalid User"))
                else {
                    if (content == null)
                        call.respond(HttpStatusCode.BadRequest, Response("Invalid Data"))
                    else {
                        NotificationService.postNotification(content, user.id)
                        call.respond(Response())
                    }
                }
            }
        }
    }
}

/** Receive a user's data from the call. */
suspend fun ApplicationCall.getUser(): User? =
        try {
            receive()
        } catch (ex: Exception) {
            null
        }