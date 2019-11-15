package dev.shog.mojor.api

import dev.shog.mojor.db.PostgreSql
import reactor.core.publisher.Flux

/**
 * Manage the health of services.
 */
object Health {
    /**
     * Get the health of the current services.I
     */
    fun getCurrentHealth(): Flux<Pair<String, Any>> =
            Flux.just(getServerHealth(), getDatabaseHealth())

    /**
     * The server is online.
     */
    private fun getServerHealth(): Pair<String, Any> =
            Pair("servers", object {
                val api = "OK"
                val cdn = "OK"
                val main = "OK"
            })

    /**
     * The database is online
     */
    private fun getDatabaseHealth(): Pair<String, Any> {
        PostgreSql.createConnection()
                ?: return Pair("database", "NOT OK")

        return Pair("database", "OK")
    }
}