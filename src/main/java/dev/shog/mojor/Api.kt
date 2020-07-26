package dev.shog.mojor

import com.fasterxml.jackson.datatype.jsonorg.JsonOrgModule
import dev.shog.lib.util.logDiscord
import dev.shog.mojor.api.RandomEmote
import dev.shog.mojor.api.blog.blogPages
import dev.shog.mojor.api.buta.butaPages
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.api.tokenInteractionPages
import dev.shog.mojor.api.users.globalUserInteractionPages
import dev.shog.mojor.api.users.userInteractionPages
import dev.shog.mojor.api.motd.motdPages
import dev.shog.mojor.handle.registerExceptions
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
import org.apache.commons.lang3.exception.ExceptionUtils
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

            registerModule(JsonOrgModule())
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
        registerExceptions()

        exception<Throwable> {
            it.printStackTrace()
            Mojor.WEBHOOK.sendBigMessage(
                    ExceptionUtils.getStackTrace(it),
                    "MOJOR\n`${ExceptionUtils.getMessage(it)}`"
            )

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
        header("Server", "Mojor")
    }

    install(CORS) {
        anyHost()

        method(HttpMethod.Options)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        method(HttpMethod.Patch)

        allowSameOrigin = true
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
        call.respond(Response(RandomEmote.getEmote()))
    }

    get("/robots.txt") {
        call.respondText("User-Agent: *\nDisallow: /")
    }

    userInteractionPages()
    tokenInteractionPages()
    globalUserInteractionPages()
    motdPages()
    blogPages()
    butaPages()
}
