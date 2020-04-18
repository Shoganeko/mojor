package dev.shog.mojor.handle.markdown

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.handle.MARKDOWN
import dev.shog.mojor.handle.modify
import dev.shog.mojor.util.UrlUtils
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.util.*

/**
 * Create a markdown page with [pageName] and [returnTo].
 */
object MarkdownPage {
    val PAGES = ArrayList<String>()

    /**
     * Get a markdown page from Github and properly format it.
     */
    fun getPage(file: String): String {
        return createHTML().html {
            head {
                title("shog.dev | ${file.removeSuffix(".md")}")

                link("${UrlUtils.URLS.cdn}/pages/markdown/css.css", "stylesheet", "text/css")
                link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
                link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")

                applyMeta()
            }

            body {
                div("content") {
                    header {
                        ul {
                            li { a(UrlUtils.URLS.main, classes = "titleA") { +"shog.dev" } }

                            li("margin")

                            li(classes = "liEntry") { a(UrlUtils.URLS.main + "/motd/history") { +"MOTD History" } }
                            li(classes = "liEntry") { a(UrlUtils.URLS.main + "/site-tree") { +"Site Tree" } }
                            li(classes = "liEntry") { a(UrlUtils.URLS.main + "/@self") { +"Account" } }
                        }
                    }

                    main {
                        comment("This is auto generated content from markdown.")

                        p { +"\$\$_INS_MD" }
                    }

                    footer {
                        p {
                            +"shog.dev ${Calendar.getInstance().get(Calendar.YEAR)} "
                            a("${UrlUtils.URLS.main}/privacy") { +"Privacy" }
                        }
                    }
                }

                script(src = "${UrlUtils.URLS.cdn}/pages/markdown/js.js") {}
            }
        }.replace("<p>\$\$_INS_MD</p>", runBlocking { getGitHubPage(file) modify UrlUtils.FORMAT })
    }

    /**
     * Get a GitHub pages and parse into a markdown page.
     */
    private suspend fun getGitHubPage(file: String): String {
        val cacheObj = Mojor.APP.getCache().getObject<String>(file)

        if (cacheObj != null) {
            Mojor.APP.getLogger().debug("Successfully found $file in cache.")
            return cacheObj.getValue()
        }

        val url = "https://raw.githubusercontent.com/Shoganeko/mojor-pages/master/$file"

        Mojor.APP.getLogger().debug("Made a request to {}.", url)

        return try {
            val fi = HttpClient().get<String>(url)

            val result = fi modify MARKDOWN
            val cache = Mojor.APP.getCache().createObject(file, result)

            cache?.getValue() ?: "Failed to retrieve content."
        } catch (ex: Exception) {
            "<h1>Invalid File!</h1>"
        }
    }
}