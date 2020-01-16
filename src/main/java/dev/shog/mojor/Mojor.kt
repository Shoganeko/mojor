package dev.shog.mojor

import dev.shog.lib.app.AppBuilder
import dev.shog.lib.cfg.ConfigHandler
import dev.shog.lib.hook.DiscordWebhook
import dev.shog.mojor.api.buta.ButaObjectHandler
import dev.shog.mojor.handle.ArgsHandler
import dev.shog.mojor.handle.file.Config
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
    const val MOJOR_VERSION = 1.0F

    val APP = AppBuilder()
            .withName("Mojor")
            .withVersion(MOJOR_VERSION)
            .checkUpdates(false)
            .usingConfig(ConfigHandler.createConfig(ConfigHandler.ConfigType.YML, "mojor", Config()))
            .withCache()
            .withWebhook { DiscordWebhook(this!!.asObject<Config>().discordUrl) }
            .build()

    var API: String = "http://localhost:8080"
    var CDN: String = "http://localhost:8070"
    var MAIN: String = "http://localhost:8090"

    /** The logger of Mojor */
    val LOGGER: Logger = LoggerFactory.getLogger("MOJOR")

    internal fun main(args: Array<String>) = runBlocking<Unit> {
        val ah = ArgsHandler()

        // Mojor Dev and Prod modes
        ah.addHooks("--prod", {
            API = "https://api.shog.dev"
            CDN = "https://cdn.shog.dev"
            MAIN = "https://shog.dev"

            Hooks.onErrorDropped {
                it.printStackTrace()

                APP
                        .sendMessage(getErrorMessage(it, true))
                        .subscribe()
            }

            runBlocking { ButaObjectHandler.init() }

            LOGGER.debug("Production mode enabled")
        }, {
            Hooks.onErrorDropped {
                it.printStackTrace()
                APP
                        .sendMessage(getErrorMessage(it, false))
                        .subscribe()
            }

            runBlocking { ButaObjectHandler.devInit() }
            Hooks.onOperatorDebug()

            LOGGER.debug("Dev mode enabled")
        })

        // If they're blocking notifications
        ah.addNonHook("--block-init-notif") {
            APP
                    .sendMessage("Started at __${SimpleDateFormat().format(Date())}__! <:PogU:644404760752947210>")
                    .subscribe()
        }

        ah.initWith(args)

        apiServer.start()
        cdnServer.start()
        mainServer.start(wait = true)
    }
}

fun main(args: Array<String>) = Mojor.main(args)