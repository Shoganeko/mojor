package dev.shog.mojor.pages

import dev.shog.mojor.handle.MarkdownModifier
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.handle.modify
import dev.shog.mojor.pages.obj.RedirectPage
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode

/** The Discord server. */
object Discord : RedirectPage {
    override fun redirect(call: ApplicationCall): String =
            "https://discord.gg/YCfeQB"
}

/** The robots.txt */
object RobotsTxt : RegPage {
    override fun getPage(call: ApplicationCall): String =
            "<pre>User-Agent: *\nDisallow: /discord\nDisallow: /motd/*</pre>"
}

/** NaM */
object Nam : RegPage {
    override fun getPage(call: ApplicationCall): String =
            "# NaM" modify MarkdownModifier
}

/** Error page with code */
class Error(private val code: Int, private val possibleFix: String = "") : RegPage {
    override fun getPage(call: ApplicationCall): String =
            MarkdownPage("error.md")
                    .respond()
                    .replace("{error-code}", code.toString())
                    .replace("{possible-fix}", possibleFix)

    override val statusCode: HttpStatusCode = HttpStatusCode.fromValue(code)
}

/** The induce error, for testing I guess. */
object InduceError : RegPage {
    override fun getPage(call: ApplicationCall): String {
        throw Exception(System.currentTimeMillis().toString())
    }
}