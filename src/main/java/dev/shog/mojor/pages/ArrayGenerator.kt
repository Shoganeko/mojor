package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlPage
import io.ktor.http.HttpStatusCode
import kotlinx.html.*

object ArrayGenerator : HtmlPage {
    override val url: String = "/argen"
    override val html: HTML.() -> Unit = {
        head {
            title("Array Generator")
            link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
            link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.0/animate.min.css", "stylesheet", "text/css")
            link("${Mojor.CDN}/pages/utility/argen.css", "stylesheet", "text/css")

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
                    +"Array Generator"
                }

                div("lang-housing") {
                    button {
                        id = "kotlin"
                        +"Kotlin"
                    }
                    +" / "
                    button {
                        id = "javascript"
                        +"JavaScript"
                    }
                }

                p("desc") {
                    +"Select either language, and add objects by simply typing them then pressing enter."
                }

                form {
                    id = "argen"

                    br
                    label {
                        input {
                            id = "arEntry"
                        }
                    }
                }

                p { id = "result"; +"arrayListOf()" }
            }

            script(src = "${Mojor.CDN}/pages/utility/argen.js") {}
        }
    }

    override val statusCode: HttpStatusCode = HttpStatusCode.OK
}