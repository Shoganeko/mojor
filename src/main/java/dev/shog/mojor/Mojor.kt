package dev.shog.mojor

import dev.shog.mojor.api.buta.ButaObjectHandler
import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.servers.apiServer
import dev.shog.mojor.servers.cdnServer
import dev.shog.mojor.servers.mainServer
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

/**
 * Mojor
 */
object Mojor {
    /**
     * The version of Mojor
     */
    const val VERSION = 1.0F

    /**
     * The logger of Mojor
     */
    val LOGGER = LoggerFactory.getLogger("MOJOR")

    internal fun main(args: Array<String>) = runBlocking<Unit> {
        ButaObjectHandler.init().subscribe()
        FileManager

        val token = TokenManager.createToken()
        println(token.token)
        token.permissions.permissions.add(Permissions.APP_MANAGER)
        apiServer.start()
        cdnServer.start()
        mainServer.start(wait = true)
    }
}

fun main(args: Array<String>) = Mojor.main(args)