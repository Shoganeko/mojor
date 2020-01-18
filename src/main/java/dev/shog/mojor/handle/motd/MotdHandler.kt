package dev.shog.mojor.handle.motd

import dev.shog.mojor.api.response.Response
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.handle.db.PostgreSql
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.options
import io.ktor.routing.post
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

/**
 * The MOTD
 */
object MotdHandler {
    /**
     * All motds
     */
    val motds = ArrayList<Motd>()

    /**
     * Register MOTD pages
     */
    fun registerPages(routing: Routing) {
        routing.options("/motd") { call.respond(Response(payload = "CORS PepeLaugh")) }

        routing.post("/motd") {
            call.isAuthorized(Permissions.MOTD_MANAGER)

            val params = call.receiveParameters()

            val owner = params["owner"]?.toLongOrNull()
            val text = params["text"]
            val date = System.currentTimeMillis()

            if (text == null || owner == null) {
                call.respond(HttpStatusCode.BadRequest, Response("Text or Owner was not included"))
                return@post
            }

            insertMotd(Motd(text, owner, date))
            call.respond(Response())
        }
    }

    /**
     * Get the recent motd.
     */
    fun getMostRecentMotd(): Motd =
            motds.last()

    /**
     * Get the oldest motd.
     */
    fun getOldestMotd(): Motd =
            motds.first()

    /**
     * Get a [Motd] by their [date].
     */
    fun getMotdByDate(date: Long): Motd? =
            motds
                    .filter { motd -> motd.date == date }
                    .getOrNull(0)

    /**
     * Insert a motd class into the database and [motds]
     */
    suspend fun insertMotd(properMotd: Motd) = coroutineScope {
        val pre = PostgreSql.createConnection()
                .prepareStatement("INSERT INTO motd.motds (data, owner, date) VALUES (?, ?, ?)")

        pre.setString(1, properMotd.data)
        pre.setLong(2, properMotd.owner)
        pre.setLong(3, properMotd.date)

        motds.add(properMotd)

        return@coroutineScope withContext(Dispatchers.Unconfined) { pre.executeUpdate() }
    }

    init {
        runBlocking {
            val rs = withContext(Dispatchers.Unconfined) {
                PostgreSql.createConnection()
                        .prepareStatement("SELECT * FROM motd.motds")
                        .executeQuery()
            }

            while (rs.next()) {
                motds.add(Motd(rs.getString("data"), rs.getLong("owner"), rs.getLong("date")))
            }
        }
    }
}