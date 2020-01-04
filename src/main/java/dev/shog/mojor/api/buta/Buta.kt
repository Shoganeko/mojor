package dev.shog.mojor.api.buta

import dev.shog.mojor.api.buta.obj.ButaObject
import dev.shog.mojor.api.buta.obj.Guild
import dev.shog.mojor.api.buta.obj.User
import dev.shog.mojor.auth.isAuthorized
import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.fancyDate
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.*
import kong.unirest.Unirest
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.io.Serializable

/**
 * Add all of the Buta pages.
 */
fun Routing.butaPages() {
    ButaSwearPage.refresh().subscribe()

    // The swears page.
    get("/v2/buta/swears") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        call.respondText(ButaSwearPage.builtJson.toString(), contentType = ContentType.Application.Json)
    }

    // The status page.
    get("/v2/buta/status") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        call.respond(ButaStatusPage.getStatusReport())
    }

    // The presences page.
    get("/v2/buta/presences") {
        call.isAuthorized(Permissions.BUTA_MANAGER)
        call.respondText(ButaPresencesPage.builtJson.toString(), contentType = ContentType.Application.Json)
    }

    // Get a ButaObject using an ID and Type
    get("/v2/buta/{id}/{type}") {
        call.isAuthorized(Permissions.BUTA_MANAGER)

        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        ButaObjectHandler.getObject(id, type)
                .switchIfEmpty(ButaObject.getEmpty().toMono())
                .map { obj ->
                    if (obj.id == 0L) {
                        launch { call.respond(HttpStatusCode.BadRequest) }
                    } else launch { call.respond(obj) }
                }
                .subscribe()
    }

    // Create a ButaObject using an ID and a Type.
    put("/v2/buta/{id}/{type}") {
        call.isAuthorized(Permissions.BUTA_MANAGER)

        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        if (!(1..2).contains(type)) {
            call.respond(HttpStatusCode.BadRequest)
            return@put
        }

        val body = when (type) {
            1 -> call.receive<Guild>()
            2 -> call.receive<User>()

            else -> {
                call.respond(HttpStatusCode.BadRequest)
                return@put
            }
        }

        if (body.id == 0L || body.id != id) { // If it's attempting to upload a body that doesn't belong to the URL ID.
            call.respond(HttpStatusCode.BadRequest)
            return@put
        }

        ButaObjectHandler.createObject(id, body)
                .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                .doOnError { launch { call.respond(HttpStatusCode.BadRequest) } }
                .subscribe()
    }

    // Delete a ButaObject using an ID and a type
    delete("/v2/buta/{id}/{type}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        ButaObjectHandler.deleteObject(id, type)
                .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                .doOnError { launch { call.respond(HttpStatusCode.BadRequest) } }
                .subscribe()
    }

    // Update a ButaObject using an ID and a type.
    patch("/v2/buta/{id}/{type}") {
        val id = call.parameters["id"]?.toLongOrNull() ?: -1L
        val type = call.parameters["type"]?.toIntOrNull() ?: -1

        if (!(1..2).contains(type)) {
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }

        val body = when (type) {
            1 -> try {
                call.receive<Guild>()
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }

            2 -> try {
                call.receive<User>()
            } catch (ex: Exception) {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }

            else -> {
                call.respond(HttpStatusCode.BadRequest)
                return@patch
            }
        }

        if (body.id == 0L || body.id != id) { // If it's attempting to upload a body that doesn't belong to the URL ID.
            call.respond(HttpStatusCode.BadRequest)
            return@patch
        }

        ButaObjectHandler.updateObject(id, body)
                .doOnSuccess { launch { call.respond(HttpStatusCode.OK) } }
                .doOnError { err ->
                    err.printStackTrace()
                    launch { call.respond(HttpStatusCode.BadRequest) }
                }
                .subscribe()
    }
}

/**
 * This gets the statistics and statuses on Buta pages and other Buta related resources.
 */
internal object ButaStatusPage {
    /**
     * The created status report by [ButaStatusPage]
     */
    class ButaStatusReport : Serializable {
        /** Mojor **/
        var server = object {
            /** The status is online */
            var status = "online"

            /** The response time, usually 0ms :) */
            var responseTime = "0ms"
        }

        /** Buta */
        var buta = object {
            /** The status could be online or offline */
            var status = "unknown"

            /** The response time, usually 0ms :) */
            var responseTime = "unknown"
        }

        /** Buta Pages */
        var pages = object {
            /** The swear words page. */
            var swear = object {
                /** The time report of the last refresh time. */
                var lastRefresed = TimeReport(ButaSwearPage.lastRefresh)

                /** The amount of swear words on the page. */
                var amount = ButaSwearPage.builtJson.length()
            }

            /** The presences page. */
            var presences = object {
                /** The time report of the last refresh time. */
                var lastRefreshed = TimeReport(ButaPresencesPage.lastRefresh)

                /** The amount of presences on the page. */
                var amount = ButaPresencesPage.builtJson.length()
            }
        }
    }

    /**
     * Create a time report with the time.
     */
    class TimeReport(time: Long) {
        /** The regular ms of the time */
        var ms = time

        /** The more readable version of the time. */
        var fancy = (System.currentTimeMillis() - time).fancyDate() + " ago"
    }

    /**
     * The last report created.
     */
    private lateinit var lastReport: ButaStatusReport

    /**
     * The last time a report was created.
     */
    private var lastReportTime = 0L

    /**
     * The amount of time that makes [lastReport] invalid
     */
    private const val REPORT_TIME_EXPIRE = 1000 * 60 * 60

    /**
     * Get [lastReport] if [lastReportTime] is under an hour.
     * If it's over an hour, create a new one and send that.
     */
    fun getStatusReport(): ButaStatusReport {
        if (System.currentTimeMillis() - lastReportTime > REPORT_TIME_EXPIRE)
            refreshStatusReport()

        return lastReport
    }

    /**
     * Refresh the current [lastReport].
     */
    private fun refreshStatusReport() {
        lastReport = ButaStatusReport() // TODO
        lastReportTime = System.currentTimeMillis()
    }
}

/**
 * The /v2/buta/swears page
 * This page retrieves it's contents by yoinking it from github user Maurice Butler, thanks bud <3
 */
internal object ButaSwearPage {
    /**
     * The full JSON array of swear words.
     */
    lateinit var builtJson: JSONArray

    /**
     * The last time [builtJson] was refreshed.
     */
    var lastRefresh: Long = 0L

    /**
     * Refresh [builtJson].
     */
    fun refresh(): Mono<Void> =
            Unirest.get("https://raw.githubusercontent.com/MauriceButler/badwords/master/array.js")
                    .asStringAsync()
                    .toMono()
                    .map { js ->
                        js.body
                                .removePrefix("module.exports = ")
                                .removeSuffix(";")
                    }
                    .map(::JSONArray)
                    .doOnNext { ar -> builtJson = ar }
                    .doFinally { lastRefresh = System.currentTimeMillis() }
                    .then()
}

/**
 * The /v2/buta/presences page
 * TODO add database interaction
 */
internal object ButaPresencesPage {
    /**
     * The full JSON array of presences.
     */
    var builtJson: JSONArray = JSONArray()
            .apply {
                put(createPresence(ACTIVITY_WATCHING, STATUS_IDLE, "Sodapoppin"))
                put(createPresence(ACTIVITY_WATCHING, STATUS_IDLE, "xQc"))
                put(createPresence(ACTIVITY_LISTENING, STATUS_ONLINE, "to you"))
                put(createPresence(ACTIVITY_WATCHING, STATUS_IDLE, "Amouranth"))
            }

    private const val ACTIVITY_PLAYING = 1
    private const val ACTIVITY_WATCHING = 2
    private const val ACTIVITY_LISTENING = 3
    private const val STATUS_ONLINE = 1
    private const val STATUS_OFFLINE = 2
    private const val STATUS_IDLE = 3
    private const val STATUS_DND = 4

    private fun createPresence(activityType: Int, statusType: Int, status: String): JSONObject =
            JSONObject()
                    .put("activityType", activityType)
                    .put("statusType", statusType)
                    .put("status", status)

    /**
     * The last time [builtJson] was refreshed.
     */
    var lastRefresh: Long = System.currentTimeMillis()
}