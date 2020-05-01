package dev.shog.mojor.pages

import dev.shog.mojor.applyMeta
import dev.shog.mojor.getSession
import dev.shog.mojor.handle.auth.AuthenticationException
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import dev.shog.mojor.handle.motd.MotdHandler
import dev.shog.mojor.pages.obj.RegPage
import dev.shog.mojor.util.UrlUtils
import io.ktor.application.ApplicationCall
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object AddGame : RegPage {
    override fun getPage(call: ApplicationCall): String {
        val ses = call.getSession() ?: throw AuthenticationException("not signed in")

        TokenHandler.getCachedToken(ses.tokenIdentifier) ?: throw AuthenticationException("not signed in")

        return createHTML().html {
            head {
                title("add game record")

                link("${UrlUtils.URLS.cdn}/pages/addGame/addGame.css", "stylesheet", "text/css")
                link("https://fonts.googleapis.com/icon?family=Material+Icons", "stylesheet", "text/css")
                link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
                link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")

                applyMeta()
            }

            body("animated fadeIn head") {
                div {
                    id = "nav"
                    a(UrlUtils.URLS.main) {
                        id = "back-button"
                        i("material-icons") { +"arrow_back" }
                    }
                }

                h1 { +"Add a Game Record" }
                div {
                    h1 { +"Win or Loss " }
                    div("radio-list") {
                        input(InputType.radio, name = "win") {
                            id = "win"
                            label { +"Win" }
                        }

                        input(InputType.radio, name = "win") {
                            id = "loss"
                            label { +"Loss" }
                        }
                    }

                    br

                    div {
                        h1 { +"Game " }

                        div("radio-list") {
                            input(InputType.radio, name = "game") {
                                id = "overwatch"
                                value = "overwatch"
                                label { +"Overwatch" }
                            }

                            input(InputType.radio, name = "game") {
                                id = "csgo"
                                value = "csgo"
                                label { +"CS:GO" }
                            }

                            input(InputType.radio, name = "game") {
                                id = "valorant"
                                value = "valorant"
                                label { +"Valorant" }
                            }
                        }
                    }

                    div("map-score") {
                        div {
                            h1 { +"Score" }
                            input(InputType.text, name = "score") { id = "score" }
                        }

                        div {
                            h1 { +"Map" }
                            input(InputType.text, name = "map") { id = "map" }
                        }
                    }

                    br
                    button(type = ButtonType.submit) {
                        id = "submit"
                        +"Add Game"
                    }

                    br

                    p { id = "status" }
                }

                script(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js") {}
                script(src = "${UrlUtils.URLS.cdn}/js/util.js") {}
                script(src = "${UrlUtils.URLS.cdn}/pages/addGame/addGame.js") {}
            }
        }.replace("{motd-content}", MotdHandler.motds.last().getProperData())
    }
}