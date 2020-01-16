package dev.shog.mojor.auth.token

import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.getJsonArray
import org.json.JSONArray

/** [User] is expired */
fun Token.isExpired() =
        TokenManager.isTokenExpired(this)

/** [Token] has [permissions] */
fun Token.hasPermissions(permissions: ArrayList<Permissions>) =
        TokenManager.hasPermissions(this, permissions)

/** Get a [Token]'s [JSONArray] permissions */
fun Token.getJsonPermissions() =
        permissions.getJsonArray()

/** Renew a [Token] */
suspend fun Token.renew() =
        TokenManager.renewToken(this)

/** Disable a [Token] */
suspend fun Token.disable() =
        TokenManager.disableToken(this)