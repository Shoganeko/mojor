package dev.shog.mojor

import dev.shog.mojor.handle.file.Config
import kong.unirest.HttpResponse
import kong.unirest.Unirest
import org.json.JSONObject
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import kotlin.system.exitProcess

/**
 * The Discord Webhook Handler
 *
 * @param iDiscordWebhookUser The webhook user to use.
 */
class DiscordWebhookHandler(private val iDiscordWebhookUser: IDiscordWebhookUser = DefaultUser) {
    /** a Discord Webhook User */
    interface IDiscordWebhookUser {
        /** The username of the Discord Webhook User */
        val username: String

        /** The avatar of the Discord Webhook User */
        val avatar: String

        /** The prefix this Discord Webhook User should have in their messages */
        val prefix: String
    }

    /**
     * The webhook URL
     */
    private var webHookUrl: String

    init {
        val url = Config.INSTANCE.discordUrl

        if (url == "")
            exitProcess(-1)

        webHookUrl = url
    }

    /**
     * Send a message through the webhook.
     */
    fun sendMessage(message: String): Mono<Void> =
            getJsonObject()
                    .doOnNext { js -> js.put("content", message) }
                    .flatMap { js -> makeRequest(js) }
                    .map { req -> parseResponse(req) ?: "" }
                    .then()

    /**
     * Build the JSON object.
     */
    private fun getJsonObject(): Mono<JSONObject> =
            JSONObject()
                    .toMono()
                    .doOnNext { js -> js.put("username", DefaultUser.username) }
                    .doOnNext { js -> js.put("avatar_url", DefaultUser.avatar) }
                    .doOnNext { js -> js.put("tts", false) }

    /**
     * Parse the response given by Discord.
     *
     * If it's null, then the request is unsuccessful.
     */
    private fun parseResponse(resp: HttpResponse<String>): String? {
        return if (resp.isSuccess)
            null
        else {
            return try {
                JSONObject(resp.body).toString()
            } catch (e: Exception) {
                null
            }
        }
    }

    /**
     * Creates the request.
     */
    private fun makeRequest(jsonObject: JSONObject): Mono<HttpResponse<String>> =
            Unirest.post(webHookUrl)
                    .header("Content-Type", "application/json")
                    .body(jsonObject.toString())
                    .asStringAsync()
                    .toMono()

    companion object {
        /** The default user for the webhooks */
        object DefaultUser : IDiscordWebhookUser {
            override val avatar: String = "https://cdn.discordapp.com/attachments/521062156024938498/636701089424605185/IMG_20191023_180024.jpg"
            override val prefix: String = ""
            override val username: String = "Mojor"
        }

        /** The default user for dev instances. */
        object DefaultDeveloperUser : IDiscordWebhookUser {
            override val avatar: String = "https://cdn.discordapp.com/attachments/521062156024938498/636701089424605185/IMG_20191023_180024.jpg"
            override val prefix: String = ""
            override val username: String = "Mojor"
        }
    }
}