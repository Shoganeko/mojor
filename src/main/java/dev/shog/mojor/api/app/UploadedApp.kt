package dev.shog.mojor.api.app

import dev.shog.mojor.compareWith
import org.json.JSONObject

/**
 * The [name] of the app, and the most recent [version] with the
 * uploaded [statistics] at [uploadedAt].
 */
data class UploadedApp(
        val name: String,
        val version: Float,
        val statistics: JSONObject,
        val uploadedAt: Long
) {
    /**
     * If the [UploadedApp]'s statistics are v
     */
    fun matchesExpected(expectedApp: ExpectedApp): Boolean =
            expectedApp.name.equals(name, true)
                    || expectedApp.expectedStatistics.compareWith(statistics)

    /**
     * If [UploadedApp] is up to date with [ExpectedApp]
     */
    fun isUpToDate(expectedApp: ExpectedApp): Boolean =
            version == expectedApp.version
}