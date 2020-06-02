package dev.shog.mojor.servers

import dev.shog.lib.util.logDiscord
import dev.shog.mojor.Mojor
import dev.shog.mojor.api.InvalidArguments
import dev.shog.mojor.api.RandomEmote
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.api.tokenInteractionPages
import dev.shog.mojor.api.users.globalUserInteractionPages
import dev.shog.mojor.api.users.userInteractionPages
import dev.shog.mojor.handle.auth.AuthenticationException
import dev.shog.mojor.api.motd.motdPages
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.*
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.JacksonConverter
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.serialization.DefaultJsonConfiguration
import io.ktor.serialization.serialization
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.text.DateFormat

/**
 * The API server
 */
@KtorExperimentalLocationsAPI
@KtorExperimentalAPI
val apiServer = embeddedServer(Netty, port = 8080, module = Application::mainModule)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
private fun Application.mainModule() {
    install(ContentNegotiation) {
        jackson {
            dateFormat = DateFormat.getDateInstance()
        }

        register(ContentType.Application.Json, JacksonConverter())

        serialization(
                contentType = ContentType.Application.Json,
                json = Json(DefaultJsonConfiguration)
        )
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(StatusPages) {
        exception<AuthenticationException> {
            call.respond(HttpStatusCode.Unauthorized, Response(it.message ?: "Authentication Exception"))
        }

        exception<InvalidArguments> {
            call.respond(HttpStatusCode.BadRequest, Response(it.message ?: "Invalid arguments."))
        }

        exception<Throwable> {
            it.printStackTrace()
            it.logDiscord(Mojor.APP)

            call.respond(HttpStatusCode.InternalServerError, Response("There was an internal error processing that request."))
        }

        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound, Response("That resource was not found."))
        }

        status(HttpStatusCode.Unauthorized) {
            call.respond(HttpStatusCode.Unauthorized, Response("You are not authorized."))
        }
    }

    install(Locations)

    install(DefaultHeaders) {
        header("Server", "Mojor/${Mojor.APP.getVersion()}")
    }

    install(CORS) {
        anyHost()
        method(HttpMethod.Options)
        header("Authorization")
        allowCredentials = true
        allowNonSimpleContentTypes = true
    }

    install(AutoHeadResponse)

    routing {
        launch { root() }
    }
}

/**
 * The main routing.
 */
private suspend fun Routing.root() {
    get("/") {
        call.respond(Response(payload = RandomEmote.getEmote()))
    }

    get("/version") {
        call.respond(Response(payload = Mojor.APP.getVersion()))
    }

    get("/robots.txt") {
        call.respondText("User-Agent: *\nDisallow: /")
    }

    userInteractionPages()
    tokenInteractionPages()
    globalUserInteractionPages()
    motdPages()
}
