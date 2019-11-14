package dev.shog.mojor.db

import dev.shog.mojor.file.Config
import reactor.core.publisher.Mono
import java.sql.Connection
import java.sql.DriverManager

/**
 * The SQL manager.
 */
object PostgreSql {
    private val URL = Config.INSTANCE.postgre.url
    private val USERNAME = Config.INSTANCE.postgre.username
    private val PASSWORD = Config.INSTANCE.postgre.password

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