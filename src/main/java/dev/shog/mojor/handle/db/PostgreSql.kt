package dev.shog.mojor.handle.db

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

/**
 * The SQL manager.
 */
object PostgreSql {
    private val connection: Connection

    private val URL: String
    private val USERNAME: String
    private val PASSWORD: String

    init {
        val cfg = Mojor.APP.getConfigObject<Config>()

        URL = cfg.postgre.url
        USERNAME = cfg.postgre.username
        PASSWORD = cfg.postgre.password


        Class.forName("org.postgresql.Driver")
        connection = DriverManager.getConnection(URL, USERNAME, PASSWORD)
    }

    /**
     * Create a connection to AWS.
     */
    fun getConnection(label: String = "Mojor"): Connection {
        Mojor.APP.logger.debug("Got connection: Label = $label")

        if (connection.isValid(5))
            return connection

        throw Exception("SQL connection timed out!")
    }
}