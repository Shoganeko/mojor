package dev.shog.mojor.pages

import dev.shog.mojor.getSession
import dev.shog.mojor.getStatisticsOfSystem
import dev.shog.mojor.handle.MARKDOWN
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.handle.modify
import dev.shog.mojor.pages.obj.RedirectPage
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall
import io.ktor.http.HttpStatusCode

/** The Discord server. */
object Discord : RedirectPage {
    override fun redirect(call: ApplicationCall): String =
            "https://discord.gg/cgGfMCd"
}

/** Buta Discord bot Invite. */
object ButaInvite : RedirectPage {
    override fun redirect(call: ApplicationCall): String =
            "https://discordapp.com/api/oauth2/authorize?client_id=605255185308319805&permissions=8&scope=bot"
}

/** The robots.txt */
object RobotsTxt : RegPage {
    override fun getPage(call: ApplicationCall): String =
            "<pre>User-Agent: *\nDisallow: /discord\nDisallow: /motd/*</pre>\nDisallow: /discord/buta"
}

/** NaM */
object Nam : RegPage {
    override fun getPage(call: ApplicationCall): String =
            "# NaM\n\n## IT IS NaM O'CLOCK" modify MARKDOWN
}

/** Error page with code */
class Error(private val code: Int, private val possibleFix: String = "") : RegPage {
    override fun getPage(call: ApplicationCall): String =
            MarkdownPage.getPage("error.md")
                    .replace("\$\$ERROR_CODE", code.toString())
                    .replace("\$\$POSSIBLE_FIX", possibleFix)

    override val statusCode: HttpStatusCode = HttpStatusCode.fromValue(code)
}

/** The induce error, for testing I guess. */
object InduceError : RegPage {
    override fun getPage(call: ApplicationCall): String {
        throw Exception(System.currentTimeMillis().toString())
    }
}

/** Debug statistics & session */
object Debug : RegPage {
    override fun getPage(call: ApplicationCall): String =
            getStatisticsOfSystem().replace("\n", "<br/>") +
                    "<br/><br/>Session: " +
                    call.getSession()
}