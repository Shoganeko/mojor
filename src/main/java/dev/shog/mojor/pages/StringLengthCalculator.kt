package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlPage
import io.ktor.http.HttpStatusCode
import kotlinx.html.*

object StringLengthCalculator : HtmlPage {
    override val url: String = "/strlen"
    override val html: HTML.() -> Unit = {
        head {
            title("String Length Calculator")
            link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
            link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.0/animate.min.css", "stylesheet", "text/css")
            link("${Mojor.CDN}/pages/utility/strlen/strlen.css", "stylesheet", "text/css")

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
                    +"Length"
                }

                form {
                    id = "strlen"

                    button {
                        type = ButtonType.submit
                        +"Execute"
                    }
                    br
                    label {
                        textArea {
                            id = "str"
                            rows = "2"
                            cols = "50"
                        }
                    }
                }

                p {
                    id = "result"
                }
            }

            script(src = "${Mojor.CDN}/pages/utility/strlen/strlen.js") {}
        }
    }

    override val statusCode: HttpStatusCode = HttpStatusCode.OK
}