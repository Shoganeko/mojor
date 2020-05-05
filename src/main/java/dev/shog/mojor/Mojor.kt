package dev.shog.mojor

import dev.shog.lib.app.AppBuilder
import dev.shog.lib.app.cfg.ConfigHandler
import dev.shog.lib.hook.DiscordWebhook
import dev.shog.lib.util.ArgsHandler
import dev.shog.lib.util.defaultFormat
import dev.shog.mojor.handle.file.Config
import dev.shog.mojor.servers.apiServer
import dev.shog.mojor.servers.cdnServer
import dev.shog.mojor.servers.mainServer
import dev.shog.mojor.util.UrlUtils.URLS
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import java.time.Instant

/**
 * Mojor
 */
object Mojor {
    const val MOJOR_VERSION = 1.5F

    val APP = AppBuilder("mojor", MOJOR_VERSION)
            .usingConfig(ConfigHandler.createConfig(ConfigHandler.ConfigType.YML, "mojor", Config()))
            .configureConfig { cfg ->
                logger = LoggerFactory.getLogger("mojor")
                webhook = DiscordWebhook(cfg.asObject<Config>().discordUrl)
                useCache = true
            }
            .build()

    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    @ExperimentalStdlibApi
    internal fun main(args: Array<String>) = runBlocking<Unit> {
        val ah = ArgsHandler()

        // Mojor Dev and Prod modes
        ah.hook("--prod") {
            URLS.api = "https://api.shog.dev"
            URLS.cdn = "https://cdn.shog.dev"
            URLS.main = "https://shog.dev"
        }

        ah.hook("--cc", ::clearCache)

        // If they're blocking notifications
        ah.nHook("--block-init-notif") {
            runBlocking { APP.sendMessage("Started at __${Instant.now().defaultFormat()}__.") }
        }

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