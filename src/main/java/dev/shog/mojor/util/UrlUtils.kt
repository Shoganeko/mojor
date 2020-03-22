package dev.shog.mojor.util

import dev.shog.mojor.Mojor
import dev.shog.mojor.handle.Modifier
import dev.shog.mojor.handle.UrlSet

/**
 * Url management.
 */
object UrlUtils {
    val FORMAT = Modifier.newModifier<String> {
        this
                .replace("\$\$URL_MAIN", URLS.main)
                .replace("\$\$URL_API", URLS.api)
                .replace("\$\$URL_CDN", URLS.cdn)
    }

    /**
     * The URLS for Mojor
     */
    val URLS = UrlSet("http://localhost:8090", "http://localhost:8080", "http://localhost:8070")
}