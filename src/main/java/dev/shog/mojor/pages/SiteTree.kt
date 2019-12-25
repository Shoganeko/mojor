package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall
import io.ktor.http.toHttpDateString

object SiteTree : RegPage {
    private val TREE = MarkdownPage("tree.md").respond()
    private var BUILT_TREE: String? = null

    override fun getPage(call: ApplicationCall): String {
        if (BUILT_TREE != null)
            return BUILT_TREE ?: ""

        BUILT_TREE = TREE
                .replace("{tree}", buildTree())
                .replace("{last-built}", System.currentTimeMillis().toHttpDateString())

        return BUILT_TREE ?: ""
    }

    /** Build the Tree */
    private fun buildTree() =
            buildString {
                for (page in MarkdownPage.PAGES) {
                    append("└ <a href=\"${Mojor.MAIN}$page\">$page</a><br/>")
                }
            }
}