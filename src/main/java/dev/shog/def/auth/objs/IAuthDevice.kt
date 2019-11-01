package dev.shog.def.auth.objs

import dev.shog.def.auth.objs.IAuthData
import reactor.core.publisher.Mono

/**
 * An auth device.
 */
interface IAuthDevice {
    /**
     * Authenticate something
     */
    suspend fun authenticate(data: IAuthData): Boolean
}