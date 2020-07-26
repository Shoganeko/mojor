package dev.shog.mojor

import dev.shog.lib.FileHandler
import dev.shog.lib.util.asBytes
import dev.shog.lib.util.asPercentage
import dev.shog.mojor.handle.InvalidArguments
import org.apache.commons.lang3.exception.ExceptionUtils
import org.w3c.dom.Document
import java.lang.management.ManagementFactory
import java.util.*

fun getUuid(id: String?): UUID {
    try {
        return UUID.fromString(id ?: throw InvalidArguments("id"))
    } catch (ex: Exception) {
        throw InvalidArguments("id")
    }
}


/** See what [first] is missing from [second] */
public fun <T> getMissing(first: Collection<T>, second: Collection<T>): Collection<T> =
        first.filter { !second.contains(it) }

/** Get system statistics */
fun getStatisticsOfSystem(): String {
    val bean = ManagementFactory.getOperatingSystemMXBean() as com.sun.management.OperatingSystemMXBean

    return "Available Processor Cores: ${Runtime.getRuntime().availableProcessors()}" +
            "\nFree Memory: ${Runtime.getRuntime().freeMemory().asBytes()}" +
            "\nUsed Memory: ${(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()).asBytes()}" +
            "\nProgram Cpu Load: ${bean.processCpuLoad.asPercentage()}" +
            "\nSys Cpu Load: ${bean.systemCpuLoad.asPercentage()}" +
            "\nMojor Version: I don't know man, ask Gradle"
}

/** Form [throwable] and [includeEveryone] into a Discord error message */
fun getErrorMessage(throwable: Throwable, includeEveryone: Boolean): String =
        buildString {
            append(if (includeEveryone) "(@everyone) : **ERROR**```" else "**ERROR**```")
            append(ExceptionUtils.getStackTrace(throwable) + "```\n\n")
            append(getStatisticsOfSystem())
        }

/**
 * Clear all files in the cache.
 */
fun clearCache() {
    FileHandler.getApplicationFolder("mojor").listFiles()
            ?.forEach { file -> file.delete() }
}