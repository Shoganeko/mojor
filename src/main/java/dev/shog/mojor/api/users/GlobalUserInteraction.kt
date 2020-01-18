package dev.shog.mojor.api.users

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.auth.user.SimpleUser
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.auth.user.delete
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.delete
import io.ktor.routing.get
import io.ktor.routing.patch

/**
 * Add the pages.
 */
fun Routing.globalUserInteractionPages() {
    /** Get all users. */
    get("/v1/users") {
        call.isAuthorized(Permissions.USER_MANAGER)
        call.respond(Response(payload = UserHolder.USERS.values))
    }

    /** Update a selected user with new user data. */
    patch("/v1/user/{id}") {
        call.isAuthorized(Permissions.USER_MANAGER)
        val id = call.parameters["id"]?.toLongOrNull()

        val user = call.getUser()

        if (user == null || user.id != id) {
            call.respond(HttpStatusCode.BadRequest, "Invalid input")
            return@patch
        }

        user.delete()
        call.respond(Response())
    }

    /** Delete a selected user. */
    delete("/v1/user/{id}") {
        call.isAuthorized(Permissions.USER_MANAGER)
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L

        val user = UserHolder.getUser(id)

        if (user == null)
            call.respond(HttpStatusCode.BadRequest, Response("Invalid user"))
        else {
            user.delete()
            call.respond(Response())
        }
    }

    /**
     * Get a selected user.
     */
    get("/v1/user/{id}") {
        call.isAuthorized(Permissions.USER_MANAGER)
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val user = UserHolder.getUser(id)

        if (user == null)
            call.respond(HttpStatusCode.BadRequest, Response("Invalid User"))
        else call.respond(Response(payload = SimpleUser.fromUser(user)))
    }
}

/** Receive a user's data from the call. */
suspend fun ApplicationCall.getUser(): User? =
        try {
            receive()
        } catch (ex: Exception) {
            null
        }