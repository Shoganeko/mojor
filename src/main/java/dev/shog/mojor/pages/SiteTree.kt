package dev.shog.mojor.pages

import dev.shog.lib.util.defaultFormat
import dev.shog.lib.util.eitherOr
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall

object SiteTree : RegPage {
    private val TREE = MarkdownPage("tree.md").respond()
    private var BUILT_TREE: String? = null

    override fun getPage(call: ApplicationCall): String {
        if (BUILT_TREE != null)
            return BUILT_TREE ?: ""

        BUILT_TREE = TREE
                .replace("{tree}", buildTree())
                .replace("{last-built}", System.currentTimeMillis().defaultFormat())

        return BUILT_TREE ?: ""
    }

    /** Build the Tree */
    private fun buildTree() =
            buildString {
                Node("", null, arrayListOf(TreeBuilder().root), false).children.forEach { child ->
                    var url = child.data

                    if (child.children.isNotEmpty()) {
                        child.children.forEach { us ->
                            url += us.toString(0)
                        }
                    }

                    append("$url<br/><br/>")
                }
            }

    /**
     * This is so incredibly scuffed, it's incredible.
     * But honestly, if it works, it works.
     */
    private class TreeBuilder {
        val root = Node.emptyNode()

        init {
            for (page in MarkdownPage.PAGES) {
                val url = page.removePrefix("/")
                var pointer: Node? = root

                url.split("/").forEach { sect ->
                    val oldPointer = pointer!!

                    pointer = oldPointer.children.lastOrNull { it.data == sect }

                    if (pointer == null) {
                        val newNode = Node(sect, oldPointer, arrayListOf())

                        oldPointer.children.add(newNode)

                        pointer = newNode
                    }
                }
            }
        }
    }

    /**
     * A node for a tree.
     *
     * @param data The data.
     * @param parent The parent node.
     * @param children The children of the node.
     */
    private data class Node(val data: String, val parent: Node?, val children: ArrayList<Node>, val includeSlash: Boolean = true) {
        fun toString(tabLevel: Int): String =
                buildString {
                    val slash = includeSlash.eitherOr("/", "")

                    if (tabLevel != 0)
                        append("<br/>${getTabLevel(tabLevel)}</span>└ ${slash}$data")
                    else append("<br/>${getTabLevel(tabLevel)}</span>${slash}$data")

                    children.forEach { child -> append(child.toString(tabLevel + 1)) }
                }

        companion object {
            fun emptyNode(): Node =
                    Node("", null, arrayListOf())
        }
    }

    /**
     * Get an amount of html tabs.
     */
    private fun getTabLevel(lvl: Int): String =
            buildString {
                for (i in 1..lvl) {
                    append("<span class=\"tab\">")
                }
            }
}