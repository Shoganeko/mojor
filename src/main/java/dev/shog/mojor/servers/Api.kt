package dev.shog.mojor.servers

import dev.shog.mojor.Mojor
import dev.shog.mojor.api.Health
import dev.shog.mojor.api.RandomEmote
import dev.shog.mojor.api.app.setAppPages
import dev.shog.mojor.auth.AuthHandler
import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.auth.hasPermissions
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.*
import io.ktor.features.*
import io.ktor.http.HttpStatusCode
import io.ktor.jackson.jackson
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.locations.Locations
import io.ktor.request.header
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.util.KtorExperimentalAPI
import io.ktor.util.toMap
import kotlinx.coroutines.launch
import org.slf4j.event.Level
import java.text.DateFormat

val apiServer = embeddedServer(Netty, port = 8080, module = Application::mainModule)

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
private fun Application.mainModule() {
    install(ContentNegotiation) {
        jackson {
            dateFormat = DateFormat.getDateInstance()
        }
    }

    install(CallLogging) {
        level = Level.INFO
    }

    install(StatusPages) {
        exception<Throwable> {
            it.printStackTrace()
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
        header("Server", "Mojor/${Mojor.VERSION}")
    }

    install(AutoHeadResponse)

    routing {
        root()
        setAppPages()
    }
}

private fun Routing.root() {
    get("/") {
        call.respond(mapOf("response" to RandomEmote.getEmote()))
    }

    get("/version") {
        call.respond(mapOf("response" to Mojor.VERSION))
    }

    get("/health") {
        // make better :D
        val token = AuthHandler.getTokenByString(call.request.parseAuthorizationHeader()?.render()?.removePrefix("token ")
                ?: "")

        if (token != null && token.hasPermissions(Permissions.APP_MANAGER)) {
            Health.getCurrentHealth()
                    .collectList()
                    .map { list -> list.toMap() }
                    .doOnNext { map ->
                        launch {
                            call.respond(map)
                        }
                    }
                    .subscribe()
            return@get
        }

        call.respond(HttpStatusCode.Unauthorized)
    }
}
