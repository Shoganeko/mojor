package dev.shog.mojor.auth

import dev.shog.mojor.auth.token.Token
import dev.shog.mojor.auth.token.TokenManager
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.auth.user.UserManager

fun Token.isExpired() = TokenManager.isTokenExpired(this)

fun Token.hasPermissions(permissions: ArrayList<Permissions>) = TokenManager.hasPermissions(this, permissions)

fun Token.getPermissions() = permissions.permissions

fun Token.getJsonPermissions() = permissions.jsonArray

fun Token.renew() = TokenManager.renewToken(this)

fun Token.disable() = TokenManager.disableToken(this)

fun User.getPermissions() = permissions.permissions

fun User.getJsonPermissions() = permissions.jsonArray

fun Token.hasPermissions(vararg permissions: Permissions) = TokenManager.hasPermissions(this, arrayListOf(*permissions))

fun User.hasPermissions(vararg permissions: Permissions) = UserManager.hasPermissions(this, arrayListOf(*permissions))

fun User.hasPermissions(permissions: ArrayList<Permissions>) = UserManager.hasPermissions(this, permissions)