package dev.shog.mojor.handle.auth.token

import dev.shog.mojor.handle.auth.obj.Permission
import org.json.JSONArray

/** [User] is expired */
fun Token.isExpired() =
        TokenManager.isTokenExpired(this)

/** [Token] has [permissions] */
fun Token.hasPermissions(permissions: ArrayList<Permission>) =
        TokenManager.hasPermissions(this, permissions)

/** Renew a [Token] */
suspend fun Token.renew() =
        TokenManager.renewToken(this)

/** Disable a [Token] */
suspend fun Token.disable() =
        TokenManager.disableToken(this)