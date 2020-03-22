package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.RegPage
import dev.shog.mojor.util.UrlUtils
import io.ktor.application.ApplicationCall
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object Login : RegPage {
    override fun getPage(call: ApplicationCall): String {
        return createHTML().html {
            head {
                title("shog.dev login")

                link("${UrlUtils.URLS.cdn}/pages/login/login.css", "stylesheet", "text/css")
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

                br

                h1("title") { +"Login" }
                form {
                    id = "login"

                    label {
                        +"Username "
                        br
                        input(InputType.text, classes = "input") { id = "username" }
                    }

                    br
                    br

                    label {
                        +"Password "
                        br
                        input(InputType.password, classes = "input") { id = "password" }
                    }


                    div("center") {
                        div {
                            id = "recaptcha"
                        }
                    }

                    br

                    button(type = ButtonType.submit) { +"Login" }
                }

                p { id = "status" }

                script(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js") {}
                script(src = "https://www.google.com/recaptcha/api.js?onload=onloadCallback&render=explicit") {
                    async = true
                    defer = true
                }
                script(src = "${UrlUtils.URLS.cdn}/js/util.js") {}
                script(src = "${UrlUtils.URLS.cdn}/js/sha.js") {}
                script(src = "${UrlUtils.URLS.cdn}/pages/login/login.js") {}
            }
        }
    }
}