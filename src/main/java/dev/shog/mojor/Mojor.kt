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
import dev.shog.mojor.util.PROD
import dev.shog.mojor.util.UrlUtils.URLS
import dev.shog.mojor.util.initNotification
import dev.shog.mojor.util.logError
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import reactor.core.publisher.Hooks

/**
 * Mojor
 */
object Mojor {
    const val MOJOR_VERSION = 1.5F

    val APP = AppBuilder()
            .withName("mojor")
            .withVersion(MOJOR_VERSION)
            .checkUpdates(false)
            .withLogger()
            .usingConfig(ConfigHandler.createConfig(ConfigHandler.ConfigType.YML, "mojor", Config()))
            .withCache()
            .withWebhook { DiscordWebhook(this!!.asObject<Config>().discordUrl) }
            .build()

    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    @ExperimentalStdlibApi
    internal fun main(args: Array<String>) = runBlocking<Unit> {
        val ah = ArgsHandler()
        Hooks.onErrorDropped(::logError)

        // Mojor Dev and Prod modes
        ah.addHooks("--prod", {
            URLS.api = "https://api.shog.dev"
            URLS.cdn = "https://cdn.shog.dev"
            URLS.main = "https://shog.dev"

            PROD = false

            runBlocking { ButaObjectHandler.init() }
        }, {
            PROD = true

            runBlocking { ButaObjectHandler.devInit() }
            Hooks.onOperatorDebug()
        })

        // If they're blocking notifications
        ah.addNonHook("--block-init-notif", ::initNotification)

        ah.initWith(args)

        apiServer.start()
        cdnServer.start()
        mainServer.start(wait = true)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@ExperimentalStdlibApi
fun main(args: Array<String>) = Mojor.main(args)