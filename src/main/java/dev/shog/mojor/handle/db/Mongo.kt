package dev.shog.mojor.handle.db

import com.mongodb.client.MongoClient
import com.mongodb.client.MongoClients
import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.file.Config
import java.lang.Exception

object Mongo {
    private var client: MongoClient? = null

    private fun makeClient() {
        val password = System.getenv("MONGO")

        client =
            MongoClients.create("mongodb+srv://mojor:${password}@shogdev.uytz5.mongodb.net/users?retryWrites=true&w=majority")
    }

    fun getClient(): MongoClient {
        if (client == null)
            makeClient()

        return client ?: throw Exception("Failed to load Mongo Client")
    }
}