package dev.shog.mojor.pages.obj

import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode

/** A page. */
interface Page {
    /** Executes the page with [applicationCall]. */
    fun exec(applicationCall: ApplicationCall)

    /** The status code for the page. */
    val statusCode: HttpStatusCode
        get() = HttpStatusCode.OK

    /** If the page should be shown on the site tree. */
    val displayTree: Boolean
        get() = true
}