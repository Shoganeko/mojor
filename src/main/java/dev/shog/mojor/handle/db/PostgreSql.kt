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
    private val URL: String
    private val USERNAME: String
    private val PASSWORD: String

    init {
        val cfg = Mojor.APP.getConfigObject<Config>()

        URL = cfg.postgre.url
        USERNAME = cfg.postgre.username
        PASSWORD = cfg.postgre.password
    }

    /**
     * Create a connection to AWS.
     */
    suspend fun createConnection(): Connection = coroutineScope {
        Class.forName("org.postgresql.Driver")
        async { DriverManager.getConnection(URL, USERNAME, PASSWORD) }
    }.await()
}