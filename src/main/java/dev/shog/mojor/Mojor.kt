package dev.shog.mojor

import dev.shog.def.App
import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.auth.user.UserManager
import dev.shog.mojor.servers.apiServer
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.digest
import io.ktor.client.request.get
import dev.shog.mojor.servers.cdnServer
import dev.shog.mojor.servers.mainServer
import io.ktor.client.HttpClient
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

    /**
     * The App.
     */
    lateinit var APP: App

    internal fun main(args: Array<String>) = runBlocking<Unit> {
//        APP = App.registerApp("Mojor", SimpleAuthData("", ""), VERSION)]
        val token = TokenManager.createToken()
        println(token.token)
                token.permissions.permissions.add(Permissions.APP_MANAGER)
        apiServer.start()
        cdnServer.start()
        mainServer.start(wait = true)
    }
}

fun main(args: Array<String>) = Mojor.main(args)