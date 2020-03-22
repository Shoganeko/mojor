package dev.shog.mojor

import dev.shog.lib.util.asBytes
import dev.shog.lib.util.asPercentage
import dev.shog.mojor.handle.auth.obj.ObjectPermissions
import dev.shog.mojor.handle.auth.obj.Session
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.pages.obj.Page
import dev.shog.mojor.util.UrlUtils
import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.ContentType
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
import org.w3c.dom.Document
import java.lang.management.ManagementFactory

/** See what [first] is missing from [second] */
fun <T> getMissing(first: Collection<T>, second: Collection<T>): Collection<T> =
        second.filterNot { first.contains(it) }

/** Creates an HTML document */
fun html(html: TagConsumer<Document>.() -> Document): Document =
        html.invoke(createHTMLDocument())

/** Apply meta to the head. */
fun HEAD.applyMeta() {
    meta("description", "welcome to shog.dev!")
    link("${UrlUtils.URLS.cdn}/favicon.jpg", "icon", "image/jpeg")
    meta("viewport", "width=device-width, initial-scale=1.0")
    meta("author", "shoganeko")
    meta("keywords", "shog,kotlin,java,shoganeko,dev,sho")
}

/** Get system statistics */
fun getStatisticsOfSystem(): String {
    val bean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

    return "Available Processor Cores: ${Runtime.getRuntime().availableProcessors()}" +
            "\nFree Memory: ${Runtime.getRuntime().freeMemory().asBytes()}" +
            "\nUsed Memory: ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).asBytes()}" +
            "\nProgram Cpu Load: ${bean.processCpuLoad.asPercentage()}" +
            "\nSys Cpu Load: ${bean.systemCpuLoad.asPercentage()}" +
            "\nMojor Version: ${Mojor.APP.getVersion()}" +
            "\nMojor URLs: ${UrlUtils.URLS.api} - ${UrlUtils.URLS.cdn} - ${UrlUtils.URLS.main}"
}

/** Get a JSON array from an [ObjectPermissions]. */
fun ObjectPermissions.getJsonArray(): JSONArray =
        JSONArray(this)

/** Form [throwable] and [includeEveryone] into a Discord error message */
fun getErrorMessage(throwable: Throwable, includeEveryone: Boolean): String =
        buildString {
            append(if (includeEveryone) "(@everyone) : **ERROR**```" else "**ERROR**```")
            append(ExceptionUtils.getStackTrace(throwable) + "```\n\n")
            append(getStatisticsOfSystem())
        }

/** Add markdown pages with name [pageName]. */
fun Routing.addMarkdownPages(vararg pageName: String) =
        pageName.asSequence()
                .forEach { name ->
                    addPages("/${name.removeSuffix(".md")}")

                    get("/${name.removeSuffix(".md")}") {
                        call.respondText(MarkdownPage.getPage(name), ContentType.parse("text/html"))
                    }
                }

/** Add each [page] to [MarkdownPage.PAGES]. */
fun addPages(vararg page: String) =
        page.asSequence()
                .forEach { pg -> MarkdownPage.PAGES.add(pg) }

/** Register [pages]. */
fun Routing.registerPages(vararg pages: Pair<String, Page>) =
        pages.asSequence()
                .forEach { page ->
                    if (page.second.displayTree)
                        addPages(page.first)

                    get(page.first) {
                        page.second.exec(call)
                    }
                }

/** Get the session */
fun ApplicationCall.getSession(): Session? =
        sessions.get<Session>()