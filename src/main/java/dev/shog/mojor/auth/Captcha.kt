package dev.shog.mojor.auth

import dev.shog.mojor.file.Config
import kong.unirest.Unirest
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/** The Captcha manager. */
object Captcha {
    /**
     * Verifies the [captcha] string.
     */
    fun verifyReCaptcha(captcha: String): Mono<Boolean> =
            Unirest.post("https://www.google.com/recaptcha/api/siteverify")
                    .field("secret", Config.INSTANCE.captchaSecret)
                    .field("response", captcha)
                    .asJsonAsync()
                    .toMono()
                    .map { obj -> obj.body.`object`.getBoolean("success") }
                    .onErrorReturn(false)
}