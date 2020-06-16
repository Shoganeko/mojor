package dev.shog.mojor.api.response

/**
 * A default response.
 *
 * @param response The server response. "OK" if nothing is to be said. But if it's an error, the error details will be described here.
 */
class Response(val response: Any? = "OK")