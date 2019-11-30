package dev.shog.mojor.api.users

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
import kotlinx.coroutines.launch

/**
 * Add the pages.
 */
fun Routing.globalUserInteractionPages() {
    /** Get all users. */
    get("/v1/users") {
        call.isAuthorized(Permissions.USER_MANAGER)
        call.respond(UserHolder.USERS.values)
    }

    /** Update a selected user with new user data. */
    patch("/v1/user/{id}") {
        call.isAuthorized(Permissions.USER_MANAGER)
        val id = call.parameters["id"]?.toLongOrNull()

        val user = call.getUser()

        if (user == null || user.id != id) {
            call.respond(HttpStatusCode.BadRequest, "Invalid input.")
            return@patch
        }

        user.delete()
                .doOnError { launch { call.respond(HttpStatusCode.BadRequest) } }
                .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                .subscribe()
    }

    /** Delete a selected user. */
    delete("/v1/user/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L

        val user = UserHolder.getUser(id)

        if (user == null)
            call.respond(HttpStatusCode.BadRequest)
        else {
            user.delete()
                    .doOnError { launch { call.respond(HttpStatusCode.BadRequest, "User does not exist!") } }
                    .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                    .subscribe()
        }
    }

    /** Get a selected user. */
    get("/v1/user/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val user = UserHolder.getUser(id)

        if (user == null)
            call.respond(HttpStatusCode.BadRequest)
        else call.respond(SimpleUser.fromUser(user))
    }
}

suspend fun ApplicationCall.getUser(): User? =
        try {
            receive()
        } catch (ex: Exception) {
            null
        }