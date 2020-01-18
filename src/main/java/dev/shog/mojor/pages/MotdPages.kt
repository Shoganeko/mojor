package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.handle.motd.MotdHandler
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall

object MotdPages {
    private val INVALID_MOTD = MarkdownPage("motd/invalidMotd.md").respond()
    private val VALID_MOTD = MarkdownPage("motd/validMotd.md").respond()
    private val MOTD_HISTORY = MarkdownPage("motd/motdHistory.md").respond()

    /**
     * Motd history using [getMotdHistory].
     */
    object History : RegPage {
        override fun getPage(call: ApplicationCall): String =
                getMotdHistory()
    }

    /**
     * Motd selector page using [getMotd].
     */
    object MotdSelector : RegPage {
        override fun getPage(call: ApplicationCall): String =
                getMotd(call.parameters["date"]?.toLongOrNull() ?: -1)

        override val displayTree: Boolean = false
    }

    /** Get a MOTD page by their [date]. */
    private fun getMotd(date: Long): String {
        if (date == -1L) return INVALID_MOTD

        val motd = MotdHandler.getMotdByDate(date)
                ?: return INVALID_MOTD

        return VALID_MOTD
                .replace("{content}", motd.getProperData())
                .replace("{author}", motd.getOwnerName())
                .replace("{date}", motd.getProperDate())
    }

    /** Get the MOTD history */
    private fun getMotdHistory(): String {
        val motds = buildString {
            append("<br/>")

            for (motd in MotdHandler.motds.reversed()) {
                append("<div>")
                append("<h4>${motd.getProperData()}<br/>")
                append("<span class=\"unh\">On ${motd.getProperDate()} by ${motd.getOwnerName()}. ")
                append("<a href=\"${Mojor.MAIN}/motd/${motd.date}\">View this MOTD</a></span></h4>")
                append("</div>")
            }
        }

        return MOTD_HISTORY
                .replace("{most-recent-motd}", MotdHandler.getMostRecentMotd().getProperDate())
                .replace("{oldest-motd}", MotdHandler.getOldestMotd().getProperDate())
                .replace("{content}", motds)
    }
}