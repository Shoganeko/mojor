package dev.shog.mojor.pages.obj

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode
import kotlinx.html.HTML

/**
 * An HTML page with access to the [ApplicationCall].
 */
interface HtmlCallPage {
    /**
     * The status code of the page.
     */
    val statusCode: HttpStatusCode

    /**
     * The HTML of the page.
     */
    fun html(call: ApplicationCall): HTML.() -> Unit

    /**
     * The URL of the page.
     */
    val url: String
}