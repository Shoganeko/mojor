package dev.shog.mojor.pages

import dev.shog.lib.util.defaultFormat
import dev.shog.mojor.api.notif.NotificationService
import dev.shog.mojor.handle.auth.obj.Permissions
import dev.shog.mojor.handle.auth.obj.Session
import dev.shog.mojor.handle.auth.token.TokenHolder
import dev.shog.mojor.handle.auth.user.User
import dev.shog.mojor.handle.auth.user.UserHolder
import dev.shog.mojor.getSession
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.pages.obj.RegPage
import dev.shog.mojor.util.UrlUtils
import io.ktor.application.ApplicationCall
import io.ktor.sessions.clear
import io.ktor.sessions.sessions
import kotlinx.coroutines.runBlocking

object Account : RegPage {
    private val ELEVATED_SIGN_IN = MarkdownPage.getPage("account/elevated-account.md")
    private val SIGNED_IN = MarkdownPage.getPage("account/signed-in.md")
    private val SIGNED_OUT = MarkdownPage.getPage("account/signed-out.md")

    override fun getPage(call: ApplicationCall): String {
        if (call.getSession() != null)
            return SignedIn.getPage(call)

        return SignedOut.getPage(call)
    }

    fun getPermissions(user: User): String =
            buildString {
                user.permissions.forEach { permissions ->
                    when (permissions) {
                        Permissions.APP_MANAGER -> append("You have the elevated dashboard. <br/>")
                        Permissions.BUTA_MANAGER -> append("You have permission to manage Buta. <br/>")
                        Permissions.USER_MANAGER -> append("You have permission to manage Users. <br/>")
                        Permissions.MOTD_MANAGER -> append("You have permission to manage MOTDS. You can do this [here](${UrlUtils.URLS.main}/motd/update).")
                    }
                }
            }

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

                val page = if (owner.permissions.contains(Permissions.APP_MANAGER)) ELEVATED_SIGN_IN else SIGNED_IN

                return page
                        .replace("\$\$NAME", owner.username)
                        .replace("\$\$DATE", ses.signInDate.defaultFormat())
                        .replace("\$\$IP", ses.signInIp)
                        .replace("\$\$PERM", getPermissions(owner))
                        .replace("\$\$NOTIF", buildString {
                            val notifs = runBlocking { NotificationService.getNotificationsForUser(owner.id) }

                            for (notif in notifs) {
                                append("\n### ${notif.data}")
                                append("\n${notif.postedAt.defaultFormat()}")
                                append("\n<button>Dismiss</button>\n\n")
                            }
                        })
            }
        }
    }
}