package dev.shog.mojor

import dev.shog.lib.FileHandler
import dev.shog.lib.util.asBytes
import dev.shog.lib.util.asPercentage
import dev.shog.mojor.handle.auth.obj.Session
import io.ktor.application.ApplicationCall
import io.ktor.sessions.get
import io.ktor.sessions.sessions
import kotlinx.html.TagConsumer
import kotlinx.html.dom.createHTMLDocument
import org.apache.commons.lang3.exception.ExceptionUtils
import org.w3c.dom.Document
import java.lang.management.ManagementFactory
import java.util.*

fun getUuid(id: String?): UUID? {
    try {
        return UUID.fromString(id ?: return null)
    } catch (ex: Exception) {
        return null
    }
}


/** See what [first] is missing from [second] */
fun <T> getMissing(first: Collection<T>, second: Collection<T>): Collection<T> =
        first.filter { !second.contains(it) }

/** Creates an HTML document */
fun html(html: TagConsumer<Document>.() -> Document): Document =
        html.invoke(createHTMLDocument())

/** Get system statistics */
fun getStatisticsOfSystem(): String {
    val bean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

    return "Available Processor Cores: ${Runtime.getRuntime().availableProcessors()}" +
            "\nFree Memory: ${Runtime.getRuntime().freeMemory().asBytes()}" +
            "\nUsed Memory: ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).asBytes()}" +
            "\nProgram Cpu Load: ${bean.processCpuLoad.asPercentage()}" +
            "\nSys Cpu Load: ${bean.systemCpuLoad.asPercentage()}" +
            "\nMojor Version: ${Mojor.APP.getVersion()}"
}

/** Form [throwable] and [includeEveryone] into a Discord error message */
fun getErrorMessage(throwable: Throwable, includeEveryone: Boolean): String =
        buildString {
            append(if (includeEveryone) "(@everyone) : **ERROR**```" else "**ERROR**```")
            append(ExceptionUtils.getStackTrace(throwable) + "```\n\n")
            append(getStatisticsOfSystem())
        }

/** Get the session */
fun ApplicationCall.getSession(): Session? =
        sessions.get<Session>()

/**
 * Clear all files in the cache.
 */
fun clearCache() {
    FileHandler.getApplicationFolder("mojor").listFiles()
            ?.forEach { file -> file.delete() }
}