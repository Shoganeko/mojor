package dev.shog.mojor.handle

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.InvalidCaptcha
import dev.shog.mojor.handle.file.Config
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kong.unirest.Unirest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.future.await
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

/**
 *  The Captcha manager.
 */
object Captcha {
    /**
     * Verifies the [captcha] string.
     */
    suspend fun verifyReCaptcha(captcha: String): Boolean = coroutineScope {
        val resp = withContext(Dispatchers.Unconfined) {
            Unirest.post("https://www.google.com/recaptcha/api/siteverify")
                .field("secret", Mojor.ENV["CAPTCHA"]!!)
                .field("response", captcha)
                .asJson()
        }

        if (resp.isSuccess) {
            val obj = resp.body.`object`

            obj.getBoolean("success")
        } else throw InvalidCaptcha()
    }
}