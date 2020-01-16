package dev.shog.mojor.auth

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.util.AttributeKey
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject

/** The Captcha manager. */
object Captcha {
    /**
     * Verifies the [captcha] string.
     */
    suspend fun verifyReCaptcha(captcha: String): Boolean = coroutineScope {
        return@coroutineScope JSONObject(HttpClient().get<String>("https://www.google.com/recaptcha/api/siteverify") {
            setAttributes {
                put(AttributeKey("secret"), Mojor.APP.getConfigObject<Config>().captchaSecret)
                put(AttributeKey("response"), captcha)
            }
        }).getBoolean("success")
    }
}