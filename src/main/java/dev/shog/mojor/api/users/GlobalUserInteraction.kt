package dev.shog.mojor.api.users

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.api.users.handle.UserManager
import dev.shog.mojor.api.users.obj.User
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.obj.Permission
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

/**
 * Add the pages.
 */
fun Routing.globalUserInteractionPages() {
    /** Get all users. */
    get("/users") {
        call.isAuthorized(Permission.USER_MANAGER)
        call.respond(UserManager.getUsers())
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


                UserManager.deleteUser(user.id)
                call.respond(Response())
            }

            /**
             * Get a selected user.
             */
            get("/user/{id}") {
                call.isAuthorized(Permission.USER_MANAGER)
                val id = call.parameters["id"]
                val user = UserManager.getUser(getUuid(id))

                call.respond(Response(user))
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