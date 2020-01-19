package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.handle.motd.MotdHandler
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object Homepage : RegPage {
    override fun getPage(call: ApplicationCall): String {
        return createHTML().html {
            head {
                title("shog.dev")

                link("${Mojor.URLS.cdn}/pages/homepage/homepage.css", "stylesheet", "text/css")
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

                            val motd = MotdHandler.getMostRecentMotd()
                            +"{motd-content}"

                            br

                            span("data") {
                                +"${motd.getProperDate()} by ${motd.getOwnerName()}"
                                br
                                a(Mojor.URLS.main + "/motd/history") { +"View History" }
                            }
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
                                        a("${Mojor.URLS.main}/strlen") { +"String Length Calculator" }

                                        +" Find the length of a string."
                                    }

                                    p("list-entry") {
                                        a("${Mojor.URLS.main}/clock") { +"Clock" }

                                        +" An online clock."
                                    }

                                    p("list-entry") {
                                        a("${Mojor.URLS.main}/argen") { +"Array Generator" }

                                        +" Generate an array for Kotlin or Javascript."
                                    }
                                }
                            }
                        }
                    }

                    div("topic") {
                        h1("topic-header") { +"contact" }

                        div("topic-info") {
                            a("https://${Mojor.URLS.main}/discord", "_blank", "contact") {
                                +"SHO#0001"
                            }
                        }
                    }
                }
                script(src = "${Mojor.URLS.cdn}/js/util.js") {}
                script(src = "${Mojor.URLS.cdn}/pages/homepage/homepage.js") {}
            }
        }.replace("{motd-content}", MotdHandler.getMostRecentMotd().getProperData())
    }
}