package dev.shog.mojor

import dev.shog.mojor.auth.obj.ObjectPermissions
import dev.shog.mojor.pages.obj.HtmlCallPage
import dev.shog.mojor.pages.obj.HtmlPage
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.Parameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.html.HEAD
import kotlinx.html.TagConsumer
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.link
import kotlinx.html.meta
import org.apache.commons.lang3.exception.ExceptionUtils
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Document
import java.lang.management.ManagementFactory
import kotlin.math.ln
import kotlin.math.pow

/** Turns ms into a seconds, day and hours format */
fun Long.fancyDate(): String {
    var response = ""

    val seconds = this / 1000

    if (seconds <= 60) {
        // Assuming there's multiple seconds
        return "$seconds seconds"
    }

    val minutes = seconds / 60

    if (minutes < 60)
        return if (minutes > 1) "$minutes minutes ${seconds - minutes * 60} seconds" else "$minutes minute ${seconds - minutes * 60} seconds"

    val hours = minutes / 60
    val hoursMinutes = minutes - hours * 60

    if (hours < 24) {
        response += if (hours > 1) "$hours hours " else "$hours hour "
        response += if (hoursMinutes > 1) "$hoursMinutes minutes" else "$hoursMinutes minute"

        return response
    }

    val days = hours / 24
    val hoursDays = hours - days * 24

    if (days < 7) {
        response += if (days > 1) "$days days " else "$days day "
        response += if (hoursDays > 1) "$hoursDays hours" else "$hoursDays hour"

        return response
    }

    val weeks = days / 7
    val weekDays = days - weeks * 7

    response += if (weeks > 1) "$weeks weeks " else "$weeks week "
    response += if (weekDays > 1) "$weekDays days" else "$weekDays day"

    return response
}

fun <T> getMissing(first: Collection<T>, second: Collection<T>): Collection<T> =
        first
                .filterNot { second.contains(it) }
                .toMutableList()

/** Creates an HTML document */
fun html(html: TagConsumer<Document>.() -> Document): Document = html.invoke(createHTMLDocument())

/** Add an [HtmlPage] */
fun Routing.add(vararg pages: HtmlPage) {
    for (page in pages)
        get(page.url) {
            call.respond(io.ktor.html.HtmlContent(page.statusCode, page.html))
        }
}


/** Does the same as [add] but requests with the [ApplicationCall]. */
fun Routing.addWithCall(vararg pages: HtmlCallPage) {
    for (page in pages) {
        get(page.url) {
            call.respond(io.ktor.html.HtmlContent(page.statusCode, page.html(call)))
        }
    }
}

/** Compare a [JSONObject] with another [JSONObject] */
fun JSONObject.compareWith(jsonObject: JSONObject): Boolean {
    for (key in jsonObject.keys())
        if (!has(key.toString()))
            return false

    return true
}

/** If a map contains [args]. */
fun Parameters.containsKeys(vararg args: String): Boolean {
    args.forEach { key ->
        if (!this.contains(key))
            return false
    }

    return true
}

/** Turn a boolean into successful or unsuccessful */
fun Boolean.success(): String =
        if (this) "successful" else "unsuccessful"

/** Apply meta to the head. */
fun HEAD.applyMeta() {
    meta("description", "welcome to shog.dev!")
    link("${Mojor.CDN}/favicon.jpg", "icon", "image/jpeg")
    meta("viewport", "width=device-width, initial-scale=1.0")
    meta("author", "shoganeko")
    meta("keywords", "shog,kotlin,java,shoganeko,dev,sho")
}

/** Get system statistics */
fun getStatisticsOfSystem(): String {
    val bean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

    return "Available Processor Cores: ${Runtime.getRuntime().availableProcessors()}" +
            "\nFree Memory: ${readableBytes(Runtime.getRuntime().freeMemory())}" +
            "\nUsed Memory: ${readableBytes(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())}" +
            "\nCpu Load: ${bean.processCpuLoad.asPercentage()}" +
            "\nSys Cpu Load: ${bean.systemCpuLoad.asPercentage()}"
}

/** Turn [bytes] into kib, mb etc. */
fun readableBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val exp = (ln(bytes.toDouble()) / ln(1024.toDouble())).toInt()
    val pre = ("KMGTPE")[exp - 1] + "i"
    return String.format("%.1f %sB", bytes / 1024.toDouble().pow(exp.toDouble()), pre)
}

/** Turn a double into a percentage. */
fun Double.asPercentage(): String =
        this.toString() +
                "%"

/** Returns true if any of the [any] are null. */
fun anyNull(vararg any: Any?): Boolean {
    any.forEach { obj ->
        if (obj == null)
            return true
    }

    return false
}

/** Get a JSON array from an [ObjectPermissions]. */
fun ObjectPermissions.getJsonArray(): JSONArray = JSONArray(permissions)

/** Form [throwable] and [includeEveryone] into a Discord error message */
fun getErrorMessage(throwable: Throwable, includeEveryone: Boolean): String {
    var msg = if (includeEveryone) "(@everyone) : **ERROR**```" else "**ERROR**```"

    msg += ExceptionUtils.getStackTrace(throwable) + "```\n\n"
    msg += getStatisticsOfSystem()

    return msg
}