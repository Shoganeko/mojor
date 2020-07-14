package dev.shog.mojor

import dev.shog.lib.app.Application
import dev.shog.lib.app.cfg.ConfigHandler
import dev.shog.lib.app.cfg.ConfigType
import dev.shog.lib.discord.DiscordWebhook
import dev.shog.lib.discord.WebhookUser
import dev.shog.lib.util.ArgsHandler
import dev.shog.lib.util.defaultFormat
import dev.shog.mojor.api.blog.BlogHandler
import dev.shog.mojor.handle.file.Config
import io.ktor.locations.KtorExperimentalLocationsAPI
import io.ktor.util.KtorExperimentalAPI
import kotlinx.coroutines.runBlocking
import org.json.JSONArray
import java.time.Instant
import java.util.*

/**
 * Mojor
 */
object Mojor {
    val APP = Application(
            "mojor",
            "1.7.0",
            ConfigHandler.useConfig(ConfigType.YML, "mojor", Config())
    ) { _, _, cfg -> DiscordWebhook(cfg.asObject<Config>().discordUrl, WebhookUser("Mojor", "https://shog.dev/favicon.png")) }

    @KtorExperimentalAPI
    @KtorExperimentalLocationsAPI
    @ExperimentalStdlibApi
    internal fun main(args: Array<String>) = runBlocking<Unit> {
        val ah = ArgsHandler()

        ah.hook("--cc", ::clearCache)

        // If they're blocking notifications
        ah.nHook("--block-init-notif") {
            runBlocking {
                APP.sendMessage("Started at __${Instant.now().defaultFormat()}__.")
            }
        }

        ah.initWith(args)

        apiServer.start(wait = true)
    }
}

@KtorExperimentalAPI
@KtorExperimentalLocationsAPI
@ExperimentalStdlibApi
fun main(args: Array<String>) = Mojor.main(args)