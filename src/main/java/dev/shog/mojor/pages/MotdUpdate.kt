package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlPage
import io.ktor.http.HttpStatusCode
import kotlinx.html.*

object MotdUpdate : HtmlPage {
    override val url: String = "/admin/motd"
    override val html: HTML.() -> Unit = {
        head {
            title("shog.dev")

            link("${Mojor.CDN}/css/motd.css", "stylesheet", "text/css")
            link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
            link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")
            applyMeta()
        }

        body("") {
            form {
                h1 { +"Update MOTD" }
                p { +"PepeLaugh requires username and password" }

                label { +"MOTD " }
                input(type = InputType.text, name = "text") { id = "text" }

                br
                br

                label { +"Username " }
                input(type = InputType.text, name = "username") { id = "username" }

                br
                br

                label { +"Password " }
                input(type = InputType.password, name = "password") { id = "password" }
                br
            }

            button {
                id = "submit"
                +"Update MOTD"
            }

            br

            p { id = "result" }

            script(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js") {}
            script(src = "${Mojor.CDN}/js/motd.js") {}
        }
    }

    override val statusCode: HttpStatusCode = HttpStatusCode.OK
}