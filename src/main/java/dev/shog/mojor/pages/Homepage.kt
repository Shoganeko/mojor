package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.HtmlPage
import io.ktor.http.HttpStatusCode
import kotlinx.html.*

object Homepage : HtmlPage {
    override val url: String = "/"
    override val html: HTML.() -> Unit = {
        head {
            title("shog.dev")

            link("${Mojor.CDN}/pages/homepage/homepage.css", "stylesheet", "text/css")

            applyMeta()
        }

        body {
            div("animated fadeIn") {
                id = "head"

                h1 {
                    id = "title"

                    +"shog.dev"
                }

                p {
                    id = "version"

                    +"v${Mojor.VERSION}"
                }

                br

                div("topic") {
                    h1("topic-header") {
                        id = "secondary-topic-header"

                        +"motd"
                    }

                    div("topic-info") {
                        id = "motd"

                        +"don't do drugs"
                    }
                }

                div("topic") {
                    h1("topic-header") {
                        id = "secondary-topic-header"

                        +"projects"
                    }

                    div("topic-info") {
                        ul {
                            li {
                                p("list-entry") {
                                    a("https://github.com/shoganeko/buta") {
                                        +"Buta"
                                    }

                                    +"- A multi-purpose Discord bot"
                                }

                                p("list-entry") {
                                    a("https://github.com/shoganeko/spotkey") {
                                        +"SpotKey"
                                    }

                                    +"- A lightweight Spotify hot-key manager."
                                }
                            }
                        }
                    }
                }

                div("topic") {
                    h1("topic-header") {
                        id = "secondary-topic-header"

                        +"utility"
                    }

                    div("topic-info") {
                        ul {
                            li {
                                p("list-entry") {
                                    a("${Mojor.MAIN}/strlen") {
                                        +"String Length Calculator"
                                    }

                                    +"- Find the length of a string."
                                }

                                p("list-entry") {
                                    a("${Mojor.MAIN}/clock") {
                                        +"Clock"
                                    }

                                    +"- An online clock."
                                }

                                p("list-entry") {
                                    a("${Mojor.MAIN}/ip") {
                                        +"IP Finder"
                                    }

                                    +"- Find your IP."
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    override val statusCode: HttpStatusCode = HttpStatusCode.OK
}