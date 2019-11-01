package dev.shog.def

import dev.shog.def.auth.SimpleAuthData
import dev.shog.def.auth.SimpleAuthDevice
import dev.shog.def.auth.objs.IUpdateDevice

/**
 * An App.
 */
class App {
    /**
     * The authorization token.
     */
    var token: String? = null

    /**
     * The version of the app.
     */
    var version: Float? = null

    /**
     * Check and update the application if applicable
     */
    suspend fun checkUpdates(updateDevice: IUpdateDevice = Update) {
        updateDevice.checkAndUpdate(
                version ?: throw Exception("Version is null!")
        )
    }

    companion object {
        suspend fun registerApp(name: String, auth: SimpleAuthData, version: Float, url: String = "https://api.shog.dev/auth"): App {
            val app = App()

            app.version = version

            val resp = SimpleAuthDevice(url, app).authenticate(auth)

            if (!resp)
                throw Exception("Failed to authenticate!")

            return app
        }
    }
}