package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.handle.motd.MotdHandler
import dev.shog.mojor.pages.obj.RegPage
import dev.shog.mojor.util.UrlUtils
import io.ktor.application.ApplicationCall
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object Homepage : RegPage {
    override fun getPage(call: ApplicationCall): String {
        return createHTML().html {
            head {
                title("shog.dev")

                link("${UrlUtils.URLS.cdn}/pages/homepage/homepage.css", "stylesheet", "text/css")
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
                                +"${motd.getDate()} by ${motd.getOwnerName()}"
                                br
                                a(UrlUtils.URLS.main + "/motd/history") { +"View History" }
                            }
                        }
                    }

                    p("links") {
                        a("https://discord.gg/R8n3T2v", "_BLANK") { +"discord" }
                        +", "
                        a("${UrlUtils.URLS.main}/utilities") { +"utilities" }
                        +", "
                        a("${UrlUtils.URLS.main}/projects") { +"projects" }
                        +"."
                    }
                }
                script(src = "${UrlUtils.URLS.cdn}/js/util.js") {}
                script(src = "${UrlUtils.URLS.cdn}/pages/homepage/homepage.js") {}
            }
        }.replace("{motd-content}", MotdHandler.getMostRecentMotd().getProperData())
    }
}