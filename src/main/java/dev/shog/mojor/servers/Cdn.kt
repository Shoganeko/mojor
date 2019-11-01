package dev.shog.mojor.servers

import dev.shog.mojor.Mojor
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.CacheControl
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.slf4j.event.Level
import java.io.FileNotFoundException
import java.lang.Exception

val cdnServer = embeddedServer(Netty, port = 8070, module = Application::mainModule)

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

    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }

        exception<Exception> {

            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not Found!"))
        }

        exception<FileNotFoundException> {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not Found!"))
        }

        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not Found!"))
        }

        status(HttpStatusCode.NotAcceptable) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not Found!"))
        }

        status(HttpStatusCode.Unauthorized) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not Authorized!"))
        }
    }

    install(Locations)

    install(DefaultHeaders) {
        header("Server", "Mojor/${Mojor.VERSION}")
    }

    install(AutoHeadResponse)

    routing {
        static {
            resources()
            defaultResource("index.html")
        }
    }
}