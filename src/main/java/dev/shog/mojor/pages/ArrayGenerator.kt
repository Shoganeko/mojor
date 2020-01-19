package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlPage
import kotlinx.html.*

object ArrayGenerator : HtmlPage {
    override val html: HTML.() -> Unit = {
        head {
            title("Array Generator")
            link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
            link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.0/animate.min.css", "stylesheet", "text/css")
            link("${Mojor.URLS.cdn}/pages/utility/argen/argen.css", "stylesheet", "text/css")

            applyMeta()
        }

        body {
            a(Mojor.URLS.main) {
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
                        id = "lang-swap"
                        +"Kotlin: Click to Swap"
                    }
                    br
                    button {
                        id = "clear"
                        +"Clear"
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

            script(src = "${Mojor.URLS.cdn}/js/util.js") {}
            script(src = "${Mojor.URLS.cdn}/pages/utility/argen/argen.js") {}
        }
    }
}