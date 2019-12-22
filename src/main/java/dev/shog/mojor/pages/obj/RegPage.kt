package dev.shog.mojor.pages.obj

import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondText
import kotlinx.coroutines.runBlocking

/** A regular [String] page. */
interface RegPage : Page {
    /** Get a page using [call]. */
    fun getPage(call: ApplicationCall): String

    override fun exec(applicationCall: ApplicationCall) {
        runBlocking {
            applicationCall.respondText(getPage(applicationCall), ContentType.parse("text/html"))
        }
    }
}