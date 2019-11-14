package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlPage
import io.ktor.http.HttpStatusCode
import kotlinx.html.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.concurrent.timerTask

object Clock : HtmlPage {
    init {
        Timer().schedule(timerTask {
            initDate = SimpleDateFormat("EEEE, MMMM d, yyyy").format(Date())
        }, 0, 1000 * 60 * 24)
    }

    override val url: String = "/clock"
    private lateinit var initDate: String
    override val html: HTML.() -> Unit = {
        head {
            title("Clock")
            link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
            link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.0/animate.min.css", "stylesheet", "text/css")
            link("${Mojor.CDN}/pages/utility/clock.css", "stylesheet", "text/css")

            applyMeta()
        }

        body {
            div {
                id = "nav"
                a(Mojor.MAIN) {
                    id = "back-button"
                    i("material-icons") { +"arrow_back" }
                }
            }

            div {
                id = "head"
                h1 {
                    id = "time"
                    +"00:00:00 PM"
                }

                h3 {
                    id = "date"
                    +initDate
                }
            }

            script(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js") {}
            script(src = "${Mojor.CDN}/pages/utility/clock.js") { }
        }
    }

    override val statusCode: HttpStatusCode = HttpStatusCode.OK
}