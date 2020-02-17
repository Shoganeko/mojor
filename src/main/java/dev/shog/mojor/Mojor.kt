package dev.shog.mojor

import dev.shog.lib.app.AppBuilder
import dev.shog.lib.cfg.ConfigHandler
import dev.shog.lib.hook.DiscordWebhook
import dev.shog.mojor.api.buta.ButaObjectHandler
import dev.shog.mojor.handle.ArgsHandler
import dev.shog.mojor.handle.UrlSet
import dev.shog.mojor.handle.file.Config
import dev.shog.mojor.servers.apiServer
import dev.shog.mojor.servers.cdnServer
import dev.shog.mojor.servers.mainServer
import io.ktor.locations.KtorExperimentalLocationsAPI
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Hooks
import java.text.SimpleDateFormat
import java.util.*

/**
 * Mojor
 */
object Mojor {
    const val MOJOR_VERSION = 1.3F

    val APP = AppBuilder()
            .withName("mojor")
            .withVersion(MOJOR_VERSION)
            .checkUpdates(false)
            .withLogger()
            .usingConfig(ConfigHandler.createConfig(ConfigHandler.ConfigType.YML, "mojor", Config()))
            .withCache()
            .withWebhook { DiscordWebhook(this!!.asObject<Config>().discordUrl) }
            .build()

    /**
     * The URLS for Mojor
     */
    var URLS = UrlSet("http://localhost:8090", "http://localhost:8080", "http://localhost:8070")

    @KtorExperimentalLocationsAPI
    @ExperimentalStdlibApi
    internal fun main(args: Array<String>) = runBlocking<Unit> {
        val ah = ArgsHandler()

        // Mojor Dev and Prod modes
        ah.addHooks("--prod", {
            URLS.api = "https://api.shog.dev"
            URLS.cdn = "https://cdn.shog.dev"
            URLS.main = "https://shog.dev"

            Hooks.onErrorDropped {
                it.printStackTrace()

                APP
                        .sendMessage(getErrorMessage(it, true))
                        .subscribe()
            }

            runBlocking { ButaObjectHandler.init() }

            APP.getLogger().debug("Production mode enabled")
        }, {
            Hooks.onErrorDropped {
                it.printStackTrace()
                APP
                        .sendMessage(getErrorMessage(it, false))
                        .subscribe()
            }

            runBlocking { ButaObjectHandler.devInit() }
            Hooks.onOperatorDebug()

            APP.getLogger().debug("Dev mode enabled")
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

@KtorExperimentalLocationsAPI
@ExperimentalStdlibApi
fun main(args: Array<String>) = Mojor.main(args)