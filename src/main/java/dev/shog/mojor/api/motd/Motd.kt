package dev.shog.mojor.api.motd

import java.util.*

/**
 * A motd class
 */
data class Motd(val data: String, val owner: UUID, val date: Long)