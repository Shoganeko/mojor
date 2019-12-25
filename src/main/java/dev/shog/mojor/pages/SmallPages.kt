package dev.shog.mojor.pages

import dev.shog.mojor.handle.MarkdownModifier
import dev.shog.mojor.handle.modify
import dev.shog.mojor.pages.obj.RedirectPage
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall

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