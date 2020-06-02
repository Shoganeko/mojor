package dev.shog.mojor.api

import java.util.stream.Collectors

/** An API exception with [message]. */
open class ApiException(message: String)
    : Exception(message)

/** Missing [missingArguments] */
class InvalidArguments(vararg missingArguments: String)
    : ApiException("Missing arguments ${missingArguments.toList().stream().collect(Collectors.joining())}")