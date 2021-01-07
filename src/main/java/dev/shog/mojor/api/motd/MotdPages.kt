package dev.shog.mojor.api.motd

import dev.shog.mojor.api.motd.response.MotdResponse
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.api.users.handle.UserManager
import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*

fun Routing.motdPages() {
    route("/motd") {
        get {
            call.respond(
                MotdHandler.motds
                    .map { MotdResponse(it, UserManager.getUser(it.owner)) }
            )
        }

        get("/latest") {
            val motd = MotdHandler.motds.last()

            call.respond(MotdResponse(motd, UserManager.getUser(motd.owner)))
        }

        get("/{motd}") {
            val date = call.parameters["motd"]?.toLongOrNull()
                ?: throw InvalidArguments("p_motd")

            val motd = MotdHandler.getMotdByDate(date)
                ?: throw NotFound("motd")

            call.respond(MotdResponse(motd, UserManager.getUser(motd.owner)))
        }

        delete("/{motd}") {
            call.isAuthorized(Permission.MOTD_MANAGER)

            val date = call.parameters["motd"]?.toLongOrNull()
                ?: throw InvalidArguments("p_motd")

            val motd = MotdHandler.getMotdByDate(date)
                ?: throw NotFound("motd")

            MotdHandler.deleteMotd(motd.date)

            call.respond(Response())
        }

        post {
            val token = call.isAuthorized(Permission.MOTD_MANAGER)

            val params = call.receiveParameters()

            val text = params["text"]
            val date = System.currentTimeMillis()

            if (text == null)
                throw InvalidArguments("owner")

            MotdHandler.insertMotd(Motd(text, token.owner, date))
            call.respond(Response())
        }
    }
}