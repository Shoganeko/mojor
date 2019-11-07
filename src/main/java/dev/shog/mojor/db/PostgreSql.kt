package dev.shog.mojor.db

import dev.shog.mojor.FileManager
import java.sql.Connection
import java.sql.DriverManager
import kotlin.system.exitProcess

/**
 * The SQL manager.
 */
object PostgreSql {
    private val URL = FileManager.get("dburi") as? String ?: exitProcess(-1)
    private val USERNAME = FileManager.get("dbu") as? String ?: exitProcess(-1)
    private val PASSWORD = FileManager.get("dbp") as? String ?: exitProcess(-1)

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
}