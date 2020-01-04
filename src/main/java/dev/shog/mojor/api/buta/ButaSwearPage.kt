package dev.shog.mojor.api.buta

import kong.unirest.Unirest
import org.json.JSONArray
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * The /v2/buta/swears page
 */
internal object ButaSwearPage {
    private var content: JSONArray? = null

    /**
     * Refresh [content].
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
                    .doOnNext { ar -> content = ar }
                    .then()

    /**
     * Get the page.
     */
    fun getPage(): JSONArray {
        if (content == null) {
            refresh()
        }

        return content ?: JSONArray()
    }
}