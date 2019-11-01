package dev.shog.mojor.pages.obj

import io.ktor.html.HtmlContent
import io.ktor.http.HttpStatusCode
import kotlinx.html.HTML

/**
 * A HTML page.
 */
interface HtmlPage {
    /**
     * The status code of the page.
     */
    val statusCode: HttpStatusCode

    /**
     * The HTML of the page.
     */
    val html: HTML.() -> Unit

    /**
     * The URL of the page.
     */
    val url: String

    /**
     * Retrieve this as [HtmlContent]
     */
    fun retrieve(): HtmlContent = HtmlContent(statusCode, html)
}