package dev.shog.mojor.servers

import dev.shog.mojor.Mojor
import dev.shog.mojor.api.Health
import dev.shog.mojor.api.RandomEmote
import dev.shog.mojor.api.buta.butaPages
import dev.shog.mojor.api.tokenInteractionPages
import dev.shog.mojor.api.users.globalUserInteractionPages
import dev.shog.mojor.api.users.userInteractionPages
import dev.shog.mojor.auth.AuthenticationException
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.getErrorMessage
import dev.shog.mojor.handle.motd.Motd
import dev.shog.mojor.handle.motd.MotdHandler
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
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
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
            call.respond(HttpStatusCode.Unauthorized, it.message ?: ":(")
        }

        exception<Throwable> {
            Mojor.WEBHOOK
                    .sendMessage("API: " + getErrorMessage(it, true))
                    .subscribe()

            call.respond(HttpStatusCode.InternalServerError)
        }

        status(HttpStatusCode.NotFound) {
            call.respond(HttpStatusCode.NotFound)
        }

        status(HttpStatusCode.Unauthorized) {
            call.respond(HttpStatusCode.Unauthorized)
        }
    }

    install(Locations)

    install(DefaultHeaders) {
        header("X-Server", "Mojor/${Mojor.VERSION}")
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
        root()
    }
}

/**
 * The main routing.
 */
private fun Routing.root() {
    get("/") {
        // TODO fix this: it doesn't work reactive-ly with the first connection
        val emote = RandomEmote.getEmote().block()

        launch {
            call.respond(mapOf("response" to emote))
        }
    }

    get("/version") {
        call.respond(mapOf("response" to Mojor.VERSION))
    }

    butaPages()
    userInteractionPages()
    tokenInteractionPages()
    globalUserInteractionPages()

    get("/robots.txt") {
        call.respondText("User-Agent: *\nDisallow: /")
    }

    options("/motd") { call.respond("CORS PepeLaugh") }

    post("/motd") {
        call.isAuthorized(Permissions.MOTD_MANAGER)

        val params = call.receiveParameters()

        val owner = params["owner"]?.toLongOrNull()
        val text = params["text"]
        val date = System.currentTimeMillis()

        if (text == null || owner == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        MotdHandler.insertMotd(Motd(text, owner, date)).subscribe()
        call.respond(HttpStatusCode.OK)
    }

    get("/health") {
        Health.getCurrentHealth()
                .collectList()
                .map { list -> list.toMap() }
                .doOnNext { map ->
                    launch {
                        call.respond(map)
                    }
                }
                .subscribe()
    }
}
