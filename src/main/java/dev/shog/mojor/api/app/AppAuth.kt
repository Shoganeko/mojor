package dev.shog.mojor.api.app

import dev.shog.mojor.Mojor.LOGGER
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/**
 * Manages the page.
 */
object AppAuth {
    /**
     * Different apps.
     */
    val VALID_APPS = ConcurrentHashMap<String, ExpectedApp>().apply {
        put("mojor", ExpectedApp("Mojor", 1.0F, JSONObject()))
        put("buta", ExpectedApp("Buta", 1.0F, JSONObject()))
    }

    /**
     * Uploaded apps.
     */
    private val UPLOADED_APPS = ConcurrentHashMap<String, UploadedApp>()

    /**
     * Replaces the current [UploadedApp] in [UPLOADED_APPS].
     */
    fun uploadApp(uploadedApp: UploadedApp): Boolean {
        val expected = VALID_APPS[uploadedApp.name]
                ?: return false

        if (!uploadedApp.matchesExpected(expected))
            return false

        if (UPLOADED_APPS.contains(uploadedApp.name.toLowerCase()))
            UPLOADED_APPS.remove(uploadedApp.name)

        UPLOADED_APPS[uploadedApp.name] = uploadedApp
        LOGGER.debug("Updating ${uploadedApp.name}:${uploadedApp.version}'s statistics/")

        return true
    }

    /**
     * Get an uploaded app in [UPLOADED_APPS].
     */
    fun getUploadedApp(name: String): UploadedApp? = UPLOADED_APPS[name]
}