package dev.shog.mojor.api

import kong.unirest.Unirest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.system.measureTimeMillis

/**
 * Random emotes
 */
object RandomEmote {
    /**
     * The emotes yoinked from FrankerFaceZ
     */
    private suspend fun refreshEmotes(pages: Int = 1) {
        println(">> Refreshing emotes with $pages pages...")

        val timeTook = measureTimeMillis {
            (1..pages)
                .forEach { getPage(it) }
        }

        println(">> Complete 5 pages! Took $timeTook ms")
    }

    private suspend fun getPage(page: Int) {
        println("> Getting emote page NO. $page")

        val timeTook = measureTimeMillis {
            val json = withContext(Dispatchers.Unconfined) {
                println("Requesting FFZ API...")

                Unirest.get("https://api.frankerfacez.com/v1/emoticons?sort=count&per_page=200&page=$page")
                    .asJson()
            }

            println("FFZ API request complete.")

            val ar = JSONObject(json.body.toString())
                .getJSONArray("emoticons")

            (0 until ar.length())
                .map {
                    val obj = ar.getJSONObject(it)

                    val imgObj = obj.getJSONObject("urls")
                    val image = imgObj.getString(imgObj.keys().asSequence().last() as String)

                    Emote(
                        obj.getString("name"),
                        image,
                        obj.getInt("height"),
                        obj.getInt("width")
                    )
                }
                .forEach { emotes.add(it) }
        }

        println("> Complete page NO. $page, took $timeTook ms.")
    }

    /**
     * The emotes yoinked from FrankerFaceZ
     */
    private val emotes: ArrayList<Emote> = arrayListOf()

    data class Emote(
        val name: String,
        val icon: String,
        val height: Int,
        val width: Int
    )

    /**
     * Get a random emote
     */
    suspend fun getEmote(): String {
        if (emotes.isEmpty())
            refreshEmotes()

        return emotes.random().name
    }

    suspend fun getEmote(emote: String): Emote? {
        if (emotes.isEmpty())
            refreshEmotes()

        return emotes.firstOrNull { it.name.equals(emote, true) }
    }
}