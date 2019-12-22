package dev.shog.mojor.pages.obj

import io.ktor.application.ApplicationCall
import io.ktor.html.HtmlContent
import io.ktor.response.respond
import kotlinx.coroutines.runBlocking
import kotlinx.html.HTML

/**
 * A HTML page.
 */
interface HtmlPage : Page {
    /**
     * The HTML of the page.
     */
    val html: HTML.() -> Unit

    override fun exec(applicationCall: ApplicationCall) {
        runBlocking {
            applicationCall.respond(HtmlContent(statusCode, html))
        }
    }
}