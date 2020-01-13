package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object Login : RegPage {
    override fun getPage(call: ApplicationCall): String {
        return createHTML().html {
            head {
                title("shog.dev login")

                link("${Mojor.CDN}/pages/login/login.css", "stylesheet", "text/css")
                link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
                link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")
                applyMeta()
            }

            body("animated fadeIn head") {
                h1("title") { +"Login" }
                form {
                    id = "login"

                    label {
                        +"Username "
                        input(InputType.text, classes = "input") { id = "username" }
                    }

                    br

                    label {
                        +"Password "
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
                script(src = "${Mojor.CDN}/js/util.js") {}
                script(src = "${Mojor.CDN}/js/sha.js") {}
                script(src = "${Mojor.CDN}/pages/login/login.js") {}
            }
        }
    }
}