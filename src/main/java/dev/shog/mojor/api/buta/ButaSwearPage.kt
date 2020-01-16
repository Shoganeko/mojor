package dev.shog.mojor.api.buta

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.json.JSONArray

/**
 * The /v2/buta/swears page
 */
internal object ButaSwearPage {
    private var content: JSONArray? = null

    /**
     * Refresh [content].
     */
    suspend fun refresh() =
            JSONArray(
                    HttpClient().get<String>("https://raw.githubusercontent.com/MauriceButler/badwords/master/array.js")
                            .removePrefix("module.exports = ")
                            .removeSuffix(";")
            )

    /**
     * Get the page.
     */
    suspend fun getPage(): JSONArray {
        if (content == null)
            refresh()

        return content ?: JSONArray()
    }
}