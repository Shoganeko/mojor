package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.motd.Motd
import dev.shog.mojor.pages.obj.HtmlPage
import io.ktor.http.HttpStatusCode
import kotlinx.html.*
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.*

object Homepage : HtmlPage {
    private val formatter = SimpleDateFormat("EEEE, MMMM d, yyyy")
    override val url: String = "/"
    override val html: HTML.() -> Unit = {
        head {
            title("shog.dev")

            link("${Mojor.CDN}/pages/homepage/homepage.css", "stylesheet", "text/css")
            link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
            link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")
            applyMeta()
        }

        body("animated fadeIn head") {
            div {
                h1("title") { +"shog.dev" }

                div("motd-topic") {
                    div("topic-info motd") {
                        id = "motd"

                        val motd = Motd.getMostRecentMotd()
                        +motd.getProperData()

                        br

                        span("data") { +"${formatter.format(Date.from(Instant.ofEpochMilli(motd.date)))} by ${motd.getOwnerName()}" }
                    }
                }

                div("topic") {
                    h1("topic-header") { +"projects" }

                    div("topic-info") {
                        ul {
                            li {
                                p("list-entry") {
                                    a("https://github.com/shoganeko/buta") { +"Buta" }

                                    +" A multi-purpose Discord bot."
                                }

                                p("list-entry") {
                                    a("https://github.com/shoganeko/spotkey") { +"SpotKey" }

                                    +" A lightweight Spotify hot-key manager."
                                }
                            }
                        }
                    }
                }

                div("topic") {
                    h1("topic-header") { +"utility" }

                    div("topic-info") {
                        ul {
                            li {
                                p("list-entry") {
                                    a("${Mojor.MAIN}/strlen") { +"String Length Calculator" }

                                    +" Find the length of a string."
                                }

                                p("list-entry") {
                                    a("${Mojor.MAIN}/clock") { +"Clock" }

                                    +" An online clock."
                                }

                                p("list-entry") {
                                    a("${Mojor.MAIN}/argen") { +"Array Generator" }

                                    +" Generate an array for Kotlin or Javascript."
                                }
                            }
                        }
                    }
                }

                div("topic") {
                    h1("topic-header") { +"contact" }

                    div("topic-info") {
                        a("https://${Mojor.MAIN}/discord", "_blank", "contact") {
                            +"SHO#0001"
                        }
                    }
                }
            }

            script(src = "${Mojor.CDN}/pages/homepage/homepage.js") {}
        }
    }

    override val statusCode: HttpStatusCode = HttpStatusCode.OK
}