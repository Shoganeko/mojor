package dev.shog.mojor.api

import kong.unirest.Unirest
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono

/**
 * Random emotes
 */
object RandomEmote {
    /**
     * The emotes yoinked from FrankerFaceZ
     */
    private val emotes = Unirest.get("https://api.frankerfacez.com/v1/emoticons")
            .queryString("sort", "count")
            .queryString("per_page", "200")
            .asJsonAsync()
            .toMono()
            .map { obj -> obj.body.`object` }
            .flatMapIterable { obj -> obj.getJSONArray("emoticons") }
            .map { js -> JSONObject(js.toString()) }
            .map { obj -> obj.getString("name") }
            .collectList()

    /**
     * Get a random emote
     */
    fun getEmote(): Mono<String> =
            emotes
                    .map { list -> list.random().toString() }
                    .onErrorReturn("xqcL")
}