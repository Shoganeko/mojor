package dev.shog.mojor

import dev.shog.lib.util.asBytes
import dev.shog.lib.util.asPercentage
import dev.shog.mojor.auth.obj.ObjectPermissions
import dev.shog.mojor.auth.obj.Session
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.pages.obj.Page
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.http.Parameters
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.html.HEAD
import kotlinx.html.TagConsumer
import kotlinx.html.dom.createHTMLDocument
import kotlinx.html.link
import kotlinx.html.meta
import org.apache.commons.lang3.exception.ExceptionUtils
import org.json.JSONArray
import org.json.JSONObject
import org.w3c.dom.Document
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import java.lang.management.ManagementFactory

/** See what [first] is missing from [second] */
fun <T> getMissing(first: Collection<T>, second: Collection<T>): Collection<T> =
        first
                .filterNot { second.contains(it) }
                .toMutableList()

/** Creates an HTML document */
fun html(html: TagConsumer<Document>.() -> Document): Document =
        html.invoke(createHTMLDocument())

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

/** Apply meta to the head. */
fun HEAD.applyMeta() {
    meta("description", "welcome to shog.dev!")
    link("${Mojor.CDN}/favicon.jpg", "icon", "image/jpeg")
    meta("viewport", "width=device-width, initial-scale=1.0")
    meta("author", "shoganeko")
    meta("keywords", "shog,kotlin,java,shoganeko,dev,sho")
}

/** Execute */
fun execute(th: Thread): Mono<Void> =
        th.run().toMono().then()

/** Get system statistics */
fun getStatisticsOfSystem(): String {
    val bean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

    return "Available Processor Cores: ${Runtime.getRuntime().availableProcessors()}" +
            "\nFree Memory: ${Runtime.getRuntime().freeMemory().asBytes()}" +
            "\nUsed Memory: ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).asBytes()}" +
            "\nProgram Cpu Load: ${bean.processCpuLoad.asPercentage()}" +
            "\nSys Cpu Load: ${bean.systemCpuLoad.asPercentage()}" +
            "\nMojor Version: ${Mojor.APP.getVersion()}" +
            "\nMojor URLs: ${Mojor.API} - ${Mojor.CDN} - ${Mojor.MAIN}"
}

/** Get a JSON array from an [ObjectPermissions]. */
fun ObjectPermissions.getJsonArray(): JSONArray =
        JSONArray(this)

/** Form [throwable] and [includeEveryone] into a Discord error message */
fun getErrorMessage(throwable: Throwable, includeEveryone: Boolean): String {
    var msg = if (includeEveryone) "(@everyone) : **ERROR**```" else "**ERROR**```"

    msg += ExceptionUtils.getStackTrace(throwable) + "```\n\n"
    msg += getStatisticsOfSystem()

    return msg
}

/** Add markdown pages with name [pageName]. */
fun Routing.addMarkdownPages(vararg pageName: String) {
    pageName.forEach { name ->
        addPages("/${name.removeSuffix(".md")}")

        get("/${name.removeSuffix(".md")}") {
            call.respondText(MarkdownPage(name).respond(), ContentType.parse("text/html"))
        }
    }
}

/** Add each [page] to [MarkdownPage.PAGES]. */
fun addPages(vararg page: String) {
    page.forEach { pg ->
        MarkdownPage.PAGES.add(pg)
    }
}

/** Register [pages]. */
fun Routing.registerPages(vararg pages: Pair<String, Page>) {
    pages.forEach { page ->
        if (page.second.displayTree)
            addPages(page.first)

        get(page.first) {
            page.second.exec(call)
        }
    }
}

/** Get the session */
fun ApplicationCall.getSession(): Session? =
        sessions.get<Session>()