package dev.shog.mojor

import dev.shog.mojor.pages.obj.HtmlCallPage
import dev.shog.mojor.pages.obj.HtmlPage
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
import org.json.JSONObject
import org.w3c.dom.Document

/**
 * Turns ms into a seconds, day and hours format
 */
fun Long.fancyDate(): String {
    var response = ""

    val seconds = this / 1000

    if (seconds <= 60) {
        // Assuming there's multiple seconds
        return "$seconds seconds"
    }

    val minutes = seconds / 60

    if (minutes < 60) {
        response = if (minutes > 1) String.format("%s minutes %s seconds", minutes, seconds - minutes * 60) else String.format("%s minute %s seconds", minutes, seconds - minutes * 60)
        return response
    }

    val hours = minutes / 60
    val hoursMinutes = minutes - hours * 60

    if (hours < 24) {
        response += if (hours > 1) String.format("%s hours ", hours) else String.format("%s hour ", hours)
        response += if (hoursMinutes > 1) String.format("%s minutes", hoursMinutes) else String.format("%s minute", hoursMinutes)
        return response
    }

    val days = hours / 24
    val hoursDays = hours - days * 24

    if (days < 7) {
        response += if (days > 1) String.format("%s days ", days) else String.format("%s day ", days)
        response += if (hoursDays > 1) String.format("%s hours", hoursDays) else String.format("%s hour", hoursDays)
        return response
    }

    val weeks = days / 7
    val weekDays = days - weeks * 7

    response += if (weeks > 1) String.format("%s weeks ", weeks) else String.format("%s week ", weeks)
    response += if (weekDays > 1) String.format("%s days", weekDays) else String.format("%s day", weekDays)
    return response
}

/**
 * Creates an HTML document
 */
fun html(html: TagConsumer<Document>.() -> Document): Document = html.invoke(createHTMLDocument())

/**
 * Add an [HtmlPage]
 */
fun Routing.add(vararg pages: HtmlPage) {
    for (page in pages)
        get(page.url) {
            call.respond(io.ktor.html.HtmlContent(page.statusCode, page.html))
        }
}


fun Routing.addWithCall(vararg pages: HtmlCallPage) {
    for (page in pages) {
        get(page.url) {
            call.respond(io.ktor.html.HtmlContent(page.statusCode, page.html(call)))
        }
    }
}

/**
 * Compare a [JSONObject] with another [JSONObject]
 */
fun JSONObject.compareWith(jsonObject: JSONObject): Boolean {
    for (key in jsonObject.keys())
        if (!has(key.toString()))
            return false

    return true
}

/**
 * If a map contains [args].
 */
fun Parameters.containsKeys(vararg args: String): Boolean {
    args.forEach { key ->
        if (!this.contains(key))
            return false
    }

    return true
}

fun Boolean.success(): String =
        if (this) "successful" else "unsuccessful"

fun HEAD.applyMeta() {
    meta("description", "welcome to shog.dev!")
    link("${Mojor.CDN}/favicon.png", "icon", "image/png")
    meta("author", "shoganeko")
    meta("keywords", "shog,kotlin,java,shoganeko,dev")
}