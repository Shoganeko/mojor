package dev.shog.def

import dev.shog.def.auth.objs.IUpdateDevice

/**
 * Checks for updates.
 */
object Update : IUpdateDevice {
    override suspend fun update() = Unit
    override suspend fun checkUpdates(version: Float): Boolean = false
}