package dev.shog.mojor.api.response

/**
 * A default response.
 *
 * @param response The server response. "OK" if nothing is to be said. But if it's an error, the error details will be described here.
 * @param payload The payload of the request. Null if void.
 */
open class Response(val response: String = "OK", val payload: Any? = null)