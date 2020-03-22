package dev.shog.mojor.pages

import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlPage
import dev.shog.mojor.util.UrlUtils
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

    private lateinit var initDate: String
    override val html: HTML.() -> Unit = {
        head {
            title("Clock")
            link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
            link("${UrlUtils.URLS.cdn}/pages/utility/clock/clock.css", "stylesheet", "text/css")

            applyMeta()
        }

        body {
            div {
                id = "nav"
                a(UrlUtils.URLS.main) {
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

            script(src = "${UrlUtils.URLS.cdn}/pages/utility/clock/clock.js") {}
        }
    }
}