package dev.shog.mojor.api.motd.response

import dev.shog.mojor.api.motd.Motd
import dev.shog.mojor.api.users.obj.User

/**
 * A response for getting an MOTD.
 */
data class MotdResponse(val motd: Motd, val owner: User?)