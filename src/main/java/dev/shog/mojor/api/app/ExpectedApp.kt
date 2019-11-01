package dev.shog.mojor.api.app

import org.json.JSONObject

/**
 * The [name] of the app, and the most recent [version].
 *
 * If it's posting statistics, if the [expectedStatistics] aren't met,
 * then it's unsuccessful.
 */
data class ExpectedApp(
        val name: String,
        val version: Float,
        val expectedStatistics: JSONObject
)