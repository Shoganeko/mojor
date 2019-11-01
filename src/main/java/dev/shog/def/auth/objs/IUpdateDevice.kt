package dev.shog.def.auth.objs

interface IUpdateDevice {
    /**
     * Check for updates using [version].
     */
    suspend fun checkUpdates(version: Float): Boolean

    /**
     * Do something to update.
     */
    suspend fun update()

    /**
     * Use [checkUpdates] to see if [version] is outdated.
     */
    suspend fun checkAndUpdate(version: Float) {
        if (checkUpdates(version))
            update()
    }
}