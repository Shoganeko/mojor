package dev.shog.mojor.api.users

import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.user.SimpleUser
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.auth.user.delete
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
    get("/v1/users") {
        call.isAuthorized(Permissions.USER_MANAGER)
        call.respond(UserHolder.USERS.values)
    }

    patch("/v1/user/{id}") {
        call.isAuthorized(Permissions.USER_MANAGER)
        val newUser = call.receive<User>() // TODO put this user as the {id} :D

        call.respond(HttpStatusCode.OK)
    }

    delete("/v1/user/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L

        val user = UserHolder.getUser(id)

        if (user == null)
            call.respond(HttpStatusCode.BadRequest)
        else {
            user.delete()
            call.respond(HttpStatusCode.OK)
        }
    }

    get("/v1/user/{id}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val user = UserHolder.getUser(id)

        if (user == null)
            call.respond(HttpStatusCode.BadRequest)
        else call.respond(SimpleUser.fromUser(user))
    }
}