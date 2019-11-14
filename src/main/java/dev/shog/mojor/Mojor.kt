package dev.shog.mojor

import dev.shog.mojor.api.buta.ButaObjectHandler
import dev.shog.mojor.file.FileManager
import dev.shog.mojor.servers.apiServer
import dev.shog.mojor.servers.cdnServer
import dev.shog.mojor.servers.mainServer
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mojor
 */
object Mojor {
    /**
     * The version of Mojor
     */
    const val VERSION = 1.0F

    var API: String = "http://localhost:8080"
    var CDN: String = "http://localhost:8070"
    var MAIN: String = "http://localhost:8090"

    /**
     * The logger of Mojor
     */
    val LOGGER = LoggerFactory.getLogger("MOJOR")

    internal fun main(args: Array<String>) = runBlocking<Unit> {
        FileManager

        val ah = ArgsHandler()

        // Mojor Dev and Prod modes
        ah.addHooks("--prod", {
            API = "http://api.shog.dev"
            CDN = "http://cdn.shog.dev"
            MAIN = "http://shog.dev"
            ButaObjectHandler.init().subscribe()
            Hooks.onErrorDropped { DiscordWebhookHandler.sendMessage("**ERROR** (@everyone): ${it.message}").subscribe() }
            DiscordWebhookHandler.init()
            DiscordWebhookHandler.sendMessage("Started at ${SimpleDateFormat().format(Date())}! <:PogU:644404760752947210>").subscribe()

            LOGGER.debug("Production mode enabled")
        }, {
            ButaObjectHandler.devInit().subscribe()
            Hooks.onOperatorDebug()
            Hooks.onErrorDropped { DiscordWebhookHandler.sendMessage("**ERROR**: ${it.message}") }
            DiscordWebhookHandler.devInit()

            LOGGER.debug("Dev mode enabled")
        })

        ah.initWith(args)

        apiServer.start()
        cdnServer.start()
        mainServer.start(wait = true)
    }
}

fun main(args: Array<String>) = Mojor.main(args)