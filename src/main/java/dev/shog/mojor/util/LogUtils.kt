package dev.shog.mojor.util

import dev.shog.lib.util.defaultFormat
import dev.shog.mojor.Mojor
import dev.shog.mojor.getErrorMessage
import java.time.Instant

/**
 * If extra debug information should be given.
 */
var PROD = false

/**
 * Output a server error for [server].
 * If [PROD] send a stack trace, if false send a @everyone.
 */
fun serverError(server: String, ex: Throwable) {
    if (PROD)
        ex.printStackTrace()

    Mojor.APP
            .sendMessage("${server}: " + getErrorMessage(ex, !PROD))
            .subscribe()
}

/**
 * Log [ex].
 */
fun logError(ex: Throwable) {
    Mojor.APP.sendMessage(getErrorMessage(ex, PROD)).subscribe()
}

/**
 * Send an initial Discord notification.
 */
fun initNotification() {
    Mojor.APP.sendMessage("Started at __${Instant.now().defaultFormat()}__.").subscribe()
}