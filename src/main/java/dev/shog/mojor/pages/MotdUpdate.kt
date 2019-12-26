package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.applyMeta
import dev.shog.mojor.auth.AuthenticationException
import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.auth.token.TokenHolder
import dev.shog.mojor.getSession
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall
import kotlinx.html.*
import kotlinx.html.stream.createHTML

object MotdUpdate : RegPage {
    override fun getPage(call: ApplicationCall): String {
        val session = call.getSession()
        val token = TokenHolder.getToken(session?.tokenIdentifier ?: "")

        if (token == null || session == null)
            throw AuthenticationException("Null")
        else {
            if (token.permissions.contains(Permissions.MOTD_MANAGER))
                return getHtml(token.token, token.owner)
            else throw AuthenticationException("NOT AUTHENTICATIO N!!!")
        }
    }

    override val displayTree: Boolean = false

    private fun getHtml(token: String, owner: Long): String {
        return createHTML().html {
            head {
                title("shog.dev")

                link("${Mojor.CDN}/pages/motd/motd.css", "stylesheet", "text/css")
                link("https://use.fontawesome.com/releases/v5.7.2/css/all.css", "stylesheet", "text/css")
                link("https://cdnjs.cloudflare.com/ajax/libs/animate.css/3.7.2/animate.min.css", "stylesheet", "text/css")
                applyMeta()
            }

            body("") {
                h1 { +"Update MOTD" }

                input(type = InputType.text, name = "text") { id = "text" }

                br

                button {
                    id = "submit"
                    +"Update MOTD"
                }

                br

                p { id = "result" }

                script(src = "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js") {}
                script(src = "${Mojor.CDN}/js/sha.js") {}
                script {
                    unsafe {
                        +("const result = document.querySelector(\"#result\");\n" +
                                "\n" +
                                "(function () {\n" +
                                "    \$.ajaxSetup({headers: {'Authorization': \"token {token}\"}});\n" +
                                "\n" +
                                "    document.querySelector(\"#submit\").addEventListener('click', function () {\n" +
                                "        result.textContent = \"Finding data...\";\n" +
                                "        const text = document.querySelector(\"#text\").value;\n" +
                                "\n" +
                                "        updateMotd(text, {owner});\n" +
                                "    });\n" +
                                "})();\n" +
                                "\n" +
                                "function updateMotd(text, owner) {\n" +
                                "    \$.post(\"https://api.shog.dev/motd\", { text: text, owner: owner }, function () {\n" +
                                "        document.querySelector(\"#result\").textContent = \"OK :)\";\n" +
                                "    });\n" +
                                "}\n")
                    }
                }
            }
        }
                .replace("{token}", token)
                .replace("{owner}", owner.toString())
    }
}