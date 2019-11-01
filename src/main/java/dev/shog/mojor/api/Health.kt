package dev.shog.mojor.api

import reactor.core.publisher.Flux

/**
 * Manage the health of services.
 */
object Health {
    /**
     * Get the health of the current services.I
     */
    fun getCurrentHealth(): Flux<Pair<String, String>> =
            Flux.just(getServerHealth())

    /**
     * The server is online.
     */
    private fun getServerHealth(): Pair<String, String> =
            Pair("server", "OK")
}