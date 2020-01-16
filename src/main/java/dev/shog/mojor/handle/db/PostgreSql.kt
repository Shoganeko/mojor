package dev.shog.mojor.handle.db

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import reactor.core.publisher.Mono
import java.sql.Connection
import java.sql.DriverManager

/**
 * The SQL manager.
 */
object PostgreSql {
    private val cfg = Mojor.APP.getConfigObject<Config>()

    private val URL = cfg.postgre.url
    private val USERNAME = cfg.postgre.username
    private val PASSWORD = cfg.postgre.password

    /**
     * Create a connection to the AWS.
     */
    fun createConnection(): Connection? {
        return try {
            DriverManager.getConnection(URL, USERNAME, PASSWORD)
        } catch (ex: Exception) {
            null
        }
    }

    /**
     * Create a connection to AWS and turn it into a [Mono].
     */
    fun monoConnection(): Mono<Connection> =
            Mono.justOrEmpty(createConnection())
}