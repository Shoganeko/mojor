package dev.shog.mojor.markdown

import dev.shog.mojor.Mojor
import dev.shog.mojor.Mojor.LOGGER
import dev.shog.mojor.applyMeta
import kong.unirest.Unirest
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import java.util.*

/**
 * Create a markdown page with [pageName] and [returnTo].
 */
class MarkdownPage(private val file: String) {
    fun respond(): String {
        return createHTML().html {
            head {
                title("shog.dev")

                link("${Mojor.CDN}/pages/markdown/css.css", "stylesheet", "text/css")
                link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
                link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")
                applyMeta()
            }

            body {
                div("content") {
                    header {
                        ul {
                            li {
                                h1 { +"shog.dev" }
                            }

                            li("margin")

                            li {
                                a(Mojor.MAIN) { +"Main Page" }
                            }
                            li {
                                a(Mojor.MAIN + "/motd/history") { +"MOTD History" }
                            }
                            li {
                                a(Mojor.MAIN + "/site-tree") { +"Site Tree" }
                            }
                        }
                    }

                    main {
                        comment("This is auto generated content from markdown.")

                        p { +"rmkdr" }
                    }

                    footer {
                        p {
                            +"shog.dev ${Calendar.getInstance().get(Calendar.YEAR)} "
                            a("${Mojor.MAIN}/privacy") { +"Privacy" }
                        }
                    }
                }

                script(src = "${Mojor.CDN}/pages/markdown/js.js") {}
            }
        }.replace("<p>rmkdr</p>", parseMarkdown(file))
    }

    companion object {
        /**
         * Pages
         */
        val PAGES = ArrayList<String>()

        /**
         * Parse a markdown string into HTML.
         */
        fun parseMarkdown(file: String): String {
            val url = "https://raw.githubusercontent.com/Shoganeko/mojor-pages/master/$file"

            LOGGER.debug("Making request to {}", url)

            val fi = try {
                Unirest
                        .get(url)
                        .asString()
            } catch (ex: Exception) {
                return "<h1>Invalid File!</h1)"
            }

            val document = Parser.builder().build().parse(fi.body)

            return HtmlRenderer
                    .builder()
                    .build()
                    .render(document)
        }
    }
}