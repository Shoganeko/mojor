package dev.shog.mojor

import dev.shog.mojor.api.buta.ButaObjectHandler
import dev.shog.mojor.file.FileManager
import dev.shog.mojor.servers.apiServer
import dev.shog.mojor.servers.cdnServer
import dev.shog.mojor.servers.mainServer
import kotlinx.coroutines.runBlocking
import org.apache.commons.lang3.SystemUtils
import org.slf4j.LoggerFactory
import reactor.core.publisher.Hooks
import java.io.File

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
        val ah = ArgsHandler()

        // Set Mojor to production mode
        ah.addHook("--prod") {
            API = "http://api.shog.dev"
            CDN = "http://cdn.shog.dev"
            MAIN = "http://shog.dev"

            LOGGER.debug("Production mode enabled")
        }

        // Delete the configuration file then initiate FileManager to rewrite it.
        ah.addHook("--force-write-cfg") {
            LOGGER.debug("Rewriting configuration file to $VERSION...")

            when {
                SystemUtils.IS_OS_WINDOWS ->
                    File(System.getenv("appdata") + "\\mojor\\config.yml")
                            .delete()

                SystemUtils.IS_OS_LINUX ->
                    File("/etc/mojor/config.yml")
                            .delete()
            }

            FileManager
        }

        ah.initWith(args)

        FileManager
        Hooks.onOperatorDebug()


        ButaObjectHandler.init().subscribe()

        apiServer.start()
        cdnServer.start()
        mainServer.start(wait = true)
    }
}

fun main(args: Array<String>) = Mojor.main(args)