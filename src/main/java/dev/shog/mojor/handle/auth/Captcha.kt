package dev.shog.mojor.handle.auth

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import kong.unirest.Unirest
import kotlinx.coroutines.coroutineScope
import org.json.JSONObject
import java.util.concurrent.CompletableFuture

/** The Captcha manager. */
object Captcha {
    /**
     * Verifies the [captcha] string.
     */
    fun verifyReCaptcha(captcha: String, minScore: Float = 0.5F): CompletableFuture<Boolean> =
            Unirest.post("https://www.google.com/recaptcha/api/siteverify")
                    .field("secret", Mojor.APP.getConfigObject<Config>().captchaSecret)
                    .field("response", captcha)
                    .asJsonAsync()
                    .handleAsync { t, u ->
                        if (t.isSuccess) {
                            val obj = t.body.`object`

                            obj.getBoolean("success") && obj.getFloat("score") >= minScore
                        } else false
                    }
}