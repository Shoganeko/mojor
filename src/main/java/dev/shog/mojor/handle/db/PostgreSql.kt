package dev.shog.mojor.handle.db

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
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
    suspend fun createConnection(): Connection = coroutineScope {
        Class.forName("org.postgresql.Driver")
        async { DriverManager.getConnection(URL, USERNAME, PASSWORD) }
    }.await()
}