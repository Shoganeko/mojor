package dev.shog.mojor.pages.obj

import io.ktor.application.ApplicationCall
import io.ktor.response.respondRedirect
import kotlinx.coroutines.runBlocking

/** A redirection page.*/
interface RedirectPage : Page {
    /** Get the page to redirect to using an [ApplicationCall]. */
    fun redirect(call: ApplicationCall): String

    override fun exec(applicationCall: ApplicationCall) {
        runBlocking {
            applicationCall.respondRedirect(redirect(applicationCall), true)
        }
    }
}