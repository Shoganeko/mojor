package dev.shog.mojor.pages

import dev.shog.mojor.Mojor
import dev.shog.mojor.auth.obj.Permissions
import dev.shog.mojor.auth.obj.Session
import dev.shog.mojor.auth.token.TokenHolder
import dev.shog.mojor.auth.user.User
import dev.shog.mojor.auth.user.UserHolder
import dev.shog.mojor.getSession
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.pages.obj.RegPage
import io.ktor.application.ApplicationCall
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import java.time.Instant
import java.util.*

object Account : RegPage {
    override fun getPage(call: ApplicationCall): String {
        if (call.getSession() != null)
            return SignedIn.getPage(call)

        return SignedOut.getPage(call)
    }

    fun getPermissions(user: User): String =
            buildString {
                user.permissions.forEach { permissions ->
                    when (permissions) {
                        Permissions.APP_MANAGER -> append("You have permissions to manage Mojor. <br/>")
                        Permissions.BUTA_MANAGER -> append("You have permission to manage Buta. <br/>")
                        Permissions.USER_MANAGER -> append("You have permission to manage Users. <br/>")
                        Permissions.MOTD_MANAGER -> append("You have permission to manage MOTDS. You can do this [here](${Mojor.MAIN}).")
                    }
                }
            }

    private val ELEVATED_SIGN_IN = MarkdownPage("account/elevated-account.md").respond()
    private val SIGNED_IN = MarkdownPage("account/signed-in.md").respond()
    private val SIGNED_OUT = MarkdownPage("account/signed-out.md").respond()

    /**
     * A signed out user.
     */
    object SignedOut : RegPage {
        override fun getPage(call: ApplicationCall): String =
                SIGNED_OUT
    }

    /**
     * A signed in user.
     */
    object SignedIn : RegPage {
        override fun getPage(call: ApplicationCall): String {
            val ses = call.getSession()
                    ?: throw Exception()

            val token = TokenHolder.getToken(ses.tokenIdentifier)

            if (token == null) {
                call.sessions.clear<Session>()
                return SIGNED_OUT
            } else {
                val owner = UserHolder.getUser(token.owner)
                        ?: return SIGNED_OUT // shouldn't ever happen

                return SIGNED_IN
                        .replace("{name}", owner.username)
                        .replace("{sign-in-date}", Homepage.formatter.format(Date.from(Instant.ofEpochMilli(ses.signInDate))))
                        .replace("{sign-in-ip}", ses.signInIp)
                        .replace("{permissions}", getPermissions(owner))
            }
        }
    }
}