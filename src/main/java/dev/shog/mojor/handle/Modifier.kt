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

    companion object {
        /**
         * Create a modifier and apply [func] to objects.
         */
        fun <A> newModifier(func: A.() -> A): Modifier<A> = object : Modifier<A> {
            override fun execute(obj: A): A =
                    func.invoke(obj)
        }
    }
}


val MARKDOWN = Modifier.newModifier<String> {
    val document = Parser.builder()
            .build()
            .parse(this)

    HtmlRenderer
            .builder()
            .build()
            .render(document)
}
val CAPITALIZE = Modifier.newModifier(String::capitalize)
val UPPERCASE = Modifier.newModifier(String::toUpperCase)
val LOWERCASE = Modifier.newModifier(String::toLowerCase)