package dev.shog.mojor.auth

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject

/** The Captcha manager. */
object Captcha {
    /**
     * Verifies the [captcha] string.
     */
    suspend fun verifyReCaptcha(captcha: String): Boolean = coroutineScope {
        val obj = JSONObject(HttpClient().submitForm<String>("https://www.google.com/recaptcha/api/siteverify", Parameters.build {
            append("secret", Mojor.APP.getConfigObject<Config>().captchaSecret)
            append("response", captcha)
        }))

        return@coroutineScope obj.getBoolean("success")
    }
}