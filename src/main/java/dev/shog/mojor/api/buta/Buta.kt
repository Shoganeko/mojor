package dev.shog.mojor.api.buta

import dev.shog.mojor.api.buta.obj.ButaObject
import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.obj.Permissions
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import kotlinx.coroutines.launch
import reactor.kotlin.core.publisher.toMono

/**
 * Add all of the Buta pages.
 */
fun Routing.butaPages() {
    ButaSwearPage.refresh().subscribe()
    ButaPresencesPage.refresh()

    // The swears page.
    get("/v2/buta/swears") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        call.respondText(ButaSwearPage.getPage().toString(), contentType = ContentType.Application.Json)
    }

    // Refresh swears
    post("/v2/buta/swears") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        ButaSwearPage.refresh().subscribe()
        call.respond(HttpStatusCode.OK)
    }

    // Refresh presences.
    post("/v2/buta/presences") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        ButaPresencesPage.refresh()
        call.respond(HttpStatusCode.OK)
    }

    // The presences page.
    get("/v2/buta/presences") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        call.respondText(ButaPresencesPage.getPage().toString(), contentType = ContentType.Application.Json)
    }

    // Get a ButaObject using an ID and Type
    get("/v2/buta/{id}/{type}") {
        call.isAuthorized(Permissions.BUTA_MANAGER)

        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        ButaObjectHandler.getObject(id, type)
                .switchIfEmpty(ButaObject.getEmpty().toMono())
                .map { obj ->
                    if (obj.id == 0L) {
                        launch { call.respond(HttpStatusCode.BadRequest) }
                    } else launch { call.respond(obj) }
                }
                .subscribe()
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
                .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                .doOnError { launch { call.respond(HttpStatusCode.BadRequest) } }
                .subscribe()
    }

    // Delete a ButaObject using an ID and a type
    delete("/v2/buta/{id}/{type}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        ButaObjectHandler.deleteObject(id, type)
                .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                .doOnError { launch { call.respond(HttpStatusCode.BadRequest) } }
                .subscribe()
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
                .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                .doOnError { err ->
                    err.printStackTrace()
                    launch { call.respond(HttpStatusCode.BadRequest) }
                }
                .subscribe()
    }
}