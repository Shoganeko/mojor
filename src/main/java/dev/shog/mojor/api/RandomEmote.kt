package dev.shog.mojor.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import org.json.JSONObject

/**
 * Random emotes
 */
object RandomEmote {
    /**
     * The emotes yoinked from FrankerFaceZ
     */
    suspend fun refreshEmotes() {
        val req = HttpClient().get<String>("https://api.frankerfacez.com/v1/emoticons?sort=count&per_page=200")

        val array = JSONObject(req).getJSONArray("emoticons")

        (0 until array.length())
                .mapTo(emotes) { array.getJSONObject(it).getString("name") }
    }

    /**
     * The emotes yoinked from FrankerFaceZ
     */
    private val emotes: ArrayList<String> = arrayListOf()

    /**
     * Get a random emote
     */
    suspend fun getEmote(): String {
        if (emotes.isEmpty())
            refreshEmotes()

        return emotes.random()
    }
}