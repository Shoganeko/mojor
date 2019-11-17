package dev.shog.mojor

import dev.shog.mojor.api.buta.ButaObjectHandler
import dev.shog.mojor.file.FileManager
import dev.shog.mojor.servers.apiServer
import dev.shog.mojor.servers.cdnServer
import dev.shog.mojor.servers.mainServer
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
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

    /** The logger of Mojor */
    val LOGGER: Logger = LoggerFactory.getLogger("MOJOR")

    /** The default Discord Webhook Handler */
    lateinit var WEBHOOK: DiscordWebhookHandler

    internal fun main(args: Array<String>) = runBlocking<Unit> {
        FileManager

        val ah = ArgsHandler()

        // Mojor Dev and Prod modes
        ah.addHooks("--prod", {
            API = "https://api.shog.dev"
            CDN = "https://cdn.shog.dev"
            MAIN = "https://shog.dev"

            WEBHOOK = DiscordWebhookHandler()
            WEBHOOK
                    .sendMessage("Started at __${SimpleDateFormat().format(Date())}__! <:PogU:644404760752947210>")
                    .subscribe()

            Hooks.onErrorDropped {
                it.printStackTrace()
                WEBHOOK
                        .sendMessage(getErrorMessage(it, true))
                        .subscribe()
            }

            ButaObjectHandler.init().subscribe()

            LOGGER.debug("Production mode enabled")
        }, {
            WEBHOOK = DiscordWebhookHandler(DiscordWebhookHandler.Companion.DefaultDeveloperUser)
            WEBHOOK
                    .sendMessage("Started at __${SimpleDateFormat().format(Date())}__! <:PogU:644404760752947210>")
                    .subscribe()

            Hooks.onErrorDropped {
                it.printStackTrace()
                WEBHOOK
                        .sendMessage(getErrorMessage(it, false))
                        .subscribe()
            }

            ButaObjectHandler.devInit().subscribe()
            Hooks.onOperatorDebug()

            LOGGER.debug("Dev mode enabled")
        })

        ah.initWith(args)

        apiServer.start()
        cdnServer.start()
        mainServer.start(wait = true)
    }
}

fun main(args: Array<String>) = Mojor.main(args)