package dev.shog.mojor.api.motd

import dev.shog.mojor.api.ApiException
import dev.shog.mojor.api.InvalidArguments
import dev.shog.mojor.api.motd.response.MotdResponse
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.user.handle.UserManager
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
                    ?: throw ApiException("MOTD could not be found!")

            call.respond(MotdResponse(motd, UserManager.getUser(motd.owner)))
        }

        delete("/{motd}") {
            call.isAuthorized(Permission.MOTD_MANAGER)

            val date = call.parameters["motd"]?.toLongOrNull()
                    ?: throw InvalidArguments("p_motd")

            val motd = MotdHandler.getMotdByDate(date)
                    ?: throw ApiException("MOTD could not be found!")

            MotdHandler.deleteMotd(motd.date)

            call.respond(Response())
        }

        post {
            call.isAuthorized(Permission.MOTD_MANAGER)

            val params = call.receiveParameters()

            val owner = getUuid(params["owner"])
            val text = params["text"]
            val date = System.currentTimeMillis()

            if (text == null || owner == null)
                throw InvalidArguments("text", "owner")

            MotdHandler.insertMotd(Motd(text, owner, date))
            call.respond(Response())
        }
    }
}