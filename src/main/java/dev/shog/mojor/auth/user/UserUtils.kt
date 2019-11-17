package dev.shog.mojor.auth.user

import dev.shog.mojor.auth.Permissions
import dev.shog.mojor.auth.token.Token
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.getJsonArray
import org.json.JSONArray

/** Get a [User]'s permissions */
fun User.getPermissions() =
        permissions.permissions

/** Get a [User]'s [JSONArray] permissions */
fun User.getJsonPermissions() =
        permissions.getJsonArray()

/** [Token] has [permissions] */
fun Token.hasPermissions(vararg permissions: Permissions) =
        TokenManager.hasPermissions(this, arrayListOf(*permissions))

/** [User] has [permissions] */
fun User.hasPermissions(vararg permissions: Permissions) =
        UserManager.hasPermissions(this, arrayListOf(*permissions))

/** [User] has [permissions] */
fun User.hasPermissions(permissions: ArrayList<Permissions>) =
        UserManager.hasPermissions(this, permissions)

/** Delete [User] */
fun User.delete() =
        UserManager.deleteUser(id)