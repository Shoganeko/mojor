package dev.shog.mojor.handle.auth.user

import dev.shog.mojor.handle.auth.obj.ObjectPermissions
import dev.shog.mojor.handle.auth.obj.Permissions
import dev.shog.mojor.handle.auth.token.Token
import dev.shog.mojor.handle.auth.token.TokenManager
import dev.shog.mojor.getJsonArray
import org.json.JSONArray

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
suspend fun User.delete() =
        UserManager.deleteUser(id)

/** Update with [user]. */
suspend fun User.updateWith(user: User) {
    UserHolder.insertUser(id, user)
    UserManager.updateUser(user)
}

/** Add [permissions] to a user's permissions.*/
suspend fun User.patchPermissions(permissions: ArrayList<Permissions>) {
    permissions.addAll(this.permissions)

    val permObj = ObjectPermissions.fromArrayList(permissions)

    val newUser = User(username, getPassword(), id, permObj, createdOn)

    UserManager.updateUser(newUser)
}