package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.ObjectPermissions

/**
 * An authorization token.
 *
 * This gives a connection permission to do something.
 * [permissions] gives the
 */
data class Token(
        val token: String,
        val permissions: ObjectPermissions,
        val createdOn: Long
)