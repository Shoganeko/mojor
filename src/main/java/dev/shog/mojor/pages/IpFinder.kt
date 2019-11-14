package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlCallPage
import io.ktor.application.ApplicationCall
import io.ktor.features.origin
import io.ktor.http.HttpStatusCode
import kotlinx.html.*

object IpFinder : HtmlCallPage {
    override val url: String = "/ip"
    override fun html(call: ApplicationCall): HTML.() -> Unit = {
        head {
            title("IP Finder")
            link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
            link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.0/animate.min.css", "stylesheet", "text/css")
            link("${Mojor.CDN}/pages/utility/ipfinder.css", "stylesheet", "text/css")

            applyMeta()
        }

        body {
            a(Mojor.MAIN) {
                i("material-icons") { +"arrow_back" }
            }

            div {
                id = "main"
                h1 {
                    id = "title"
                    +"IP"
                }
                p {
                    id = "subtitle"
                    +"This result is not saved!"
                }
                p {
                    id = "result"
                    +"Your IP is "

                    span {
                        id = "ip"
                        +call.request.origin.remoteHost
                    }
                }
            }

            script(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js") {}
        }
    }

    override val statusCode: HttpStatusCode = HttpStatusCode.OK
}