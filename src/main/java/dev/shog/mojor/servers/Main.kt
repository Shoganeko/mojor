package dev.shog.mojor.servers

import dev.shog.mojor.Mojor
import dev.shog.mojor.add
import dev.shog.mojor.addWithCall
import dev.shog.mojor.pages.Clock
import dev.shog.mojor.pages.Homepage
import dev.shog.mojor.pages.IpFinder
import dev.shog.mojor.pages.StringLengthCalculator
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
import io.ktor.response.respond
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
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


    install(StatusPages) {
        exception<Throwable> {
            call.respond(HttpStatusCode.InternalServerError)
        }

        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound, mapOf("error" to "Not Found!"))
        }

        status(HttpStatusCode.Unauthorized) {
            call.respond(HttpStatusCode.Unauthorized, mapOf("error" to "Not Authorized!"))
        }
    }

    install(Locations)

    install(DefaultHeaders) {
        header("X-Server", "Mojor/${Mojor.VERSION}")
    }

    routing {
        add(Homepage, Clock, StringLengthCalculator)
        addWithCall(IpFinder)
    }
}