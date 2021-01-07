package dev.shog.mojor

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import dev.shog.lib.discord.DiscordWebhook
import dev.shog.lib.discord.WebhookUser
import dev.shog.lib.util.ArgsHandler
import io.github.cdimascio.dotenv.Dotenv
import io.ktor.locations.*
import io.ktor.util.*
import kotlinx.coroutines.runBlocking
import org.mindrot.jbcrypt.BCrypt
import org.slf4j.LoggerFactory

/**
 * Mojor
 */
object Mojor {
    var BASE = "http://localhost:8080"
    var FRONTEND_BASE = "http://localost:4000"

    val ENV = Dotenv.load()

    val WEBHOOK = DiscordWebhook(ENV["WEBHOOK"]!!, WebhookUser("Mojor", "https://shog.dev/favicon.png"))

    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    @ExperimentalStdlibApi
    internal fun main(args: Array<String>) = runBlocking<Unit> {
        // mute mongodb
        val loggerContext = LoggerFactory.getILoggerFactory() as LoggerContext
        val rootLogger = loggerContext.getLogger("org.mongodb.driver")
        rootLogger.level = Level.OFF

        val ah = ArgsHandler()

        ah.hook("--cc", ::clearCache)

        ah.hook("--prod") {
            BASE = "https://api.shog.dev"
            FRONTEND_BASE = "https://shog.dev"
        }

        ah.initWith(args)

        apiServer.start(wait = true)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@ExperimentalStdlibApi
fun main(args: Array<String>) = Mojor.main(args)