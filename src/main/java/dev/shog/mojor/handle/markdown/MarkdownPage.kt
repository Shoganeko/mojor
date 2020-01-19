package dev.shog.mojor.handle.markdown

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.handle.MarkdownModifier
import dev.shog.mojor.handle.modify
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.util.*

/**
 * Create a markdown page with [pageName] and [returnTo].
 */
class MarkdownPage(private val file: String) {
    fun respond(): String {
        return createHTML().html {
            head {
                title("shog.dev")

                link("${Mojor.URLS.cdn}/pages/markdown/css.css", "stylesheet", "text/css")
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
                                a(Mojor.URLS.main) { +"Main Page" }
                            }
                            li {
                                a(Mojor.URLS.main + "/motd/history") { +"MOTD History" }
                            }
                            li {
                                a(Mojor.URLS.main + "/site-tree") { +"Site Tree" }
                            }
                            li {
                                a(Mojor.URLS.main + "/account") { +"Account" }
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
                            a("${Mojor.URLS.main}/privacy") { +"Privacy" }
                        }
                    }
                }

                script(src = "${Mojor.URLS.cdn}/pages/markdown/js.js") {}
            }
        }.replace("<p>rmkdr</p>", runBlocking { getGitHubPage(file) })
    }

    companion object {
        /**
         * Pages
         */
        val PAGES = ArrayList<String>()

        /**
         * Get a GitHub pages and parse into a markdown page.
         */
        suspend fun getGitHubPage(file: String): String {
            val cacheObj = Mojor.APP.getCache().getObject<String>(file)

            if (cacheObj != null) {
                Mojor.APP.getLogger().debug("Successfully found $file in cache.")
                return cacheObj.getValue()
            }

            val url = "https://raw.githubusercontent.com/Shoganeko/mojor-pages/master/$file"

            Mojor.APP.getLogger().debug("Made a request to {}.", url)

            val fi = try {
                HttpClient().get<String>(url)
            } catch (ex: Exception) {
                return "<h1>Invalid File!</h1)"
            }

            val result = fi modify MarkdownModifier
            val cache = Mojor.APP.getCache().createObject(file, result)

            return cache?.getValue() ?: "Failed to retrieve content."
        }
    }
}