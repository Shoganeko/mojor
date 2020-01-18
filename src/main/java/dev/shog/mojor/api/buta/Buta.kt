package dev.shog.mojor.api.buta

import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.obj.Permissions
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.routing.*

/**
 * Add all of the Buta pages.
 */
suspend fun Routing.butaPages() {
    ButaSwearPage.refresh()
    ButaPresencesPage.refresh()

    // The swears page.
    get("/v2/buta/swears") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        call.respond(Response(payload = ButaSwearPage.getPage().toString()))
    }

    // Refresh swears
    post("/v2/buta/swears") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        ButaSwearPage.refresh()
        call.respond(Response())
    }

    // Refresh presences.
    post("/v2/buta/presences") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        ButaPresencesPage.refresh()
        call.respond(Response())
    }

    // The presences page.
    get("/v2/buta/presences") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        call.respond(Response(payload = ButaPresencesPage.getPage().toString()))
    }

    // Get a ButaObject using an ID and Type
    get("/v2/buta/{id}/{type}") {
        call.isAuthorized(Permissions.BUTA_MANAGER)

        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1
        val obj = ButaObjectHandler.getObject(id, type)

        if (obj == null)
            call.respond(HttpStatusCode.BadRequest, Response("Invalid ID or Type"))
        else call.respond(Response(payload = obj))
    }

    // Create a ButaObject using an ID and a Type.
    put("/v2/buta/{id}/{type}") {
        call.isAuthorized(Permissions.BUTA_MANAGER)

        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        if (!(1..2).contains(type)) {
            call.respond(HttpStatusCode.BadRequest)
            return@put
        }

        val body = when (type) {
            1 -> call.receive<Guild>()
            2 -> call.receive<User>()

            else -> {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
        }

        if (body.id == 0L || body.id != id) { // If it's attempting to upload a body that doesn't belong to the URL ID.
            call.respond(HttpStatusCode.BadRequest)
            return@put
        }

        ButaObjectHandler.createObject(id, body)
        call.respond(Response())
    }

    // Delete a ButaObject using an ID and a type
    delete("/v2/buta/{id}/{type}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        ButaObjectHandler.deleteObject(id, type)
        call.respond(Response())
    }

    // Update a ButaObject using an ID and a type.
    patch("/v2/buta/{id}/{type}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        if (!(1..2).contains(type)) {
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }

        val body = when (type) {
            1 -> try {
                call.receive<Guild>()
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }

            2 -> try {
                call.receive<User>()
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }

            else -> {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }
        }

        if (body.id == 0L || body.id != id) { // If it's attempting to upload a body that doesn't belong to the URL ID.
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }

        ButaObjectHandler.updateObject(id, body)
        call.respond(Response())
    }
}