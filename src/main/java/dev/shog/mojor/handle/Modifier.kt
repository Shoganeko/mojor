package dev.shog.mojor.handle

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer


/**
 * Utilize a [Modifier]
 */
infix fun <A> A.modify(modifier: Modifier<A>): A =
        modifier.execute(this)

/**
 * An object modifier
 */
interface Modifier<A> {
    fun execute(obj: A): A
}

/**
 * Modifies a string to html markdown
 */
object MarkdownModifier : Modifier<String> {
    override fun execute(obj: String): String {
        val document = Parser.builder()
                .build()
                .parse(obj)

        return HtmlRenderer
                .builder()
                .build()
                .render(document)
    }
}