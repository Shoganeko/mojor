package dev.shog.mojor.pages

import dev.shog.lib.util.defaultFormat
import dev.shog.mojor.applyMeta
import dev.shog.mojor.getSession
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import dev.shog.mojor.handle.auth.user.handle.UserManager
import dev.shog.mojor.handle.game.GameHandler
import dev.shog.mojor.pages.obj.Page
import dev.shog.mojor.util.UrlUtils
import io.ktor.application.ApplicationCall
import io.ktor.http.ContentType
import io.ktor.response.respondRedirect
import io.ktor.response.respondText
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.util.*

/**
 * View a user's game record.
 */
object GameRecord : Page {
    fun getPage(uuid: UUID): String {
        val name = UserManager.getUser(uuid)?.username

        return createHTML().html {
            head {
                title("game record")

                link("${UrlUtils.URLS.cdn}/pages/gameRecord/gameRecord.css", "stylesheet", "text/css")
                link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
                link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
                link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")

                applyMeta()
            }

            body("animated fadeIn head") {
                div {
                    id = "nav"
                    a(UrlUtils.URLS.main) {
                        id = "back-button"
                        i("material-icons") { +"arrow_back" }
                    }
                }

                h1 { +"${name}'s game record" }

                div {
                    id = "controls"

                    a {
                        id = "refresh"
                        span("material-icons") { +"refresh" }
                    }

                    a {
                        id = "add"
                        span("material-icons") { +"add" }
                    }

                }

                div {
                    id = "games"
                }

                script(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js") {}
                script(src = "${UrlUtils.URLS.cdn}/js/util.js") {}
                script(src = "${UrlUtils.URLS.cdn}/pages/gameRecord/gameRecord.js") {}
            }
        }
    }

    override fun exec(applicationCall: ApplicationCall) {
        val user = applicationCall.parameters["user"]

        if (user == "self") {
            val ses = applicationCall.getSession()

            if (ses == null)
                runBlocking { applicationCall.respondRedirect("${UrlUtils.URLS.main}/@self") }
            else {
                val id = TokenHandler.getCachedToken(ses.tokenIdentifier)?.owner!!

                runBlocking {
                    applicationCall.respondText(
                            getPage(id),
                            ContentType.parse("text/html")
                    )
                }
            }

            return
        }

        val userObj = UserManager.getUser(user)

        if (userObj == null) {
            runBlocking { applicationCall.respondRedirect("${UrlUtils.URLS.main}/@self") }
            return
        }

        runBlocking {
            applicationCall.respondText(
                    getPage(userObj.id),
                    ContentType.parse("text/html")
            )
        }
    }
}