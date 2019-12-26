package dev.shog.mojor.servers

import dev.shog.mojor.Mojor
import dev.shog.mojor.addMarkdownPages
import dev.shog.mojor.auth.obj.Session
import dev.shog.mojor.auth.token.TokenHolder
import dev.shog.mojor.handle.MarkdownModifier
import dev.shog.mojor.handle.modify
import dev.shog.mojor.pages.*
import dev.shog.mojor.registerPages
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.CachingOptions
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.sessions.*
import org.slf4j.event.Level

val mainServer = embeddedServer(Netty, port = 8090, module = Application::mainModule)

@KtorExperimentalLocationsAPI
private fun Application.mainModule() {
    install(ContentNegotiation)

    install(CachingHeaders) {
        options { outgoingContent ->
            val def = CachingOptions(CacheControl.MaxAge(maxAgeSeconds = 24 * 60 * 60))
            when (outgoingContent.contentType?.withoutParameters()) {
                ContentType.Text.CSS -> def
                ContentType.Text.JavaScript -> def
                else -> null
            }
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(Sessions) {
        cookie<Session>("SESSION", SessionStorageMemory()) {
            cookie.path = "/"
        }
    }

    install(StatusPages) {
        exception<Throwable> {
            val errorString = it.message ?: "Error"

            Mojor.WEBHOOK
                    .sendMessage("There has been an error on the Main server!\n$errorString")
                    .subscribe()

            Error(500, "There's an issue with our backend. **If you have a chance, please report this [here](https://shog.dev/discord).**" modify MarkdownModifier).exec(call)
        }

        status(HttpStatusCode.NotFound) {
            Error(404, "Make sure that you have entered the correct URL!").exec(call)
        }

        status(HttpStatusCode.NotAcceptable) {
            Error(406).exec(call)
        }

        status(HttpStatusCode.Unauthorized) {
            Error(401, "Make sure you have logged in!").exec(call)
        }
    }

    install(Locations)

    install(DefaultHeaders) {
        header("X-Server", "Mojor/${Mojor.VERSION}")
    }

    routing {
        post("/session") {
            val params = call.receiveParameters()

            if (params.contains("token") && TokenHolder.hasToken(params["token"])) {
                call.sessions.set(Session(params["token"]!!, System.currentTimeMillis(), call.request.origin.remoteHost))
                call.respond("Token has been updated with the proper token.")
            } else call.respond(HttpStatusCode.BadRequest, "${params["token"]} is an invalid token!")
        }

        addMarkdownPages("privacy.md")

        registerPages(
                "/site-tree" to SiteTree,
                "/motd/history" to MotdPages.History,
                "/motd/{date}" to MotdPages.MotdSelector,
                "/discord" to Discord,
                "/robots.txt" to RobotsTxt,
                "/" to Homepage,
                "/motd/update" to MotdUpdate,
                "/clock" to Clock,
                "/strlen" to StringLengthCalculator,
                "/argen" to ArrayGenerator,
                "/nam" to Nam,
                "/induce/error" to InduceError,
                "/login" to Login,
                "/account" to Account,
                "/debug" to Debug
        )
    }
}