package dev.shog.mojor.api.buta

import dev.shog.mojor.api.buta.api.DiscordApi
import dev.shog.mojor.api.buta.data.ButaDataHandler
import dev.shog.mojor.api.buta.response.GetGuildResponse
import dev.shog.mojor.api.buta.bot.ButaInteraction
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.InvalidAuthorization
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.auth.parseAuthorizationHeader
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import io.ktor.routing.*

fun Routing.butaPages() {
    route("buta") {
        get("/callback") {
            val params = call.request.queryParameters
            val code = params["code"] ?: throw InvalidArguments("code")

            val token = ButaHandler.getToken(code)

            call.respondRedirect("http://localhost:4000/buta/login?token=${token.id}")
        }

        get("/guilds") {
            val discord = call.isDiscordAuthenticated()

            val guilds = DiscordApi.getGuilds(discord)
                    .filter { guild -> guild.administrator }

            call.respond(guilds)
        }

        get("/self") {
            val discord = call.isDiscordAuthenticated()

            call.respond(DiscordApi.getIdentity(discord))
        }

        route("/guild") {
            get {
                val discord = call.isDiscordAuthenticated()

                val params = call.request.queryParameters
                val guild = params["guild"] ?: throw InvalidArguments("guild")

                val discordGuild = DiscordApi.getGuilds(discord)
                        .singleOrNull { discordGuild -> discordGuild.id == guild }
                        ?: throw InvalidArguments("guild")

                call.respond(GetGuildResponse(
                        discordGuild,
                        ButaDataHandler.getGuild(guild.toLong())
                ))
            }

            get("/roles") {
                val discord = call.isDiscordAuthenticated()

                val params = call.request.queryParameters
                val guild = params["guild"] ?: throw InvalidArguments("guild")

                val discordGuild = DiscordApi.getGuilds(discord)
                        .singleOrNull { discordGuild -> discordGuild.id == guild }
                        ?: throw InvalidArguments("guild")

                val roles = ButaInteraction.getRoles(discordGuild.id.toLong())

                call.respondText(
                        roles.toString(),
                        ContentType.Application.Json,
                        HttpStatusCode.OK
                )
            }

            patch {
                val discord = call.isDiscordAuthenticated()

                val params = call.receiveParameters()
                val guild = params["guild"] ?: throw InvalidArguments("guild")
                val type = params["type"] ?: throw InvalidArguments("type")
                val value = params["value"] ?: throw InvalidArguments("value")

                val discordGuild = DiscordApi.getGuilds(discord)
                        .singleOrNull { discordGuild -> discordGuild.id == guild }
                        ?: throw InvalidArguments("guild")

                ButaDataHandler.setObject(discordGuild.id.toLong(), type, value)

                call.respond(Response())
            }
        }
    }
}

@Throws(InvalidAuthorization::class)
private fun ApplicationCall.isDiscordAuthenticated(): DiscordToken {
    val header = request.parseAuthorizationHeader()
            ?.render()
            ?.split(" ")

    if (header == null || header[0] != "Bearer")
        throw InvalidAuthorization("Header or token type is invalid.")

    return TokenHandler.getToken(header[1])
}