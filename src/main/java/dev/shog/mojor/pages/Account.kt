package dev.shog.mojor.pages

import dev.shog.lib.util.defaultFormat
import dev.shog.mojor.api.notif.NotificationService
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.obj.Session
import dev.shog.mojor.getSession
import dev.shog.mojor.handle.auth.token.handle.TokenHandler
import dev.shog.mojor.handle.auth.user.handle.UserManager
import dev.shog.mojor.handle.auth.user.obj.User
import dev.shog.mojor.handle.markdown.MarkdownPage
import dev.shog.mojor.handle.modify
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
    private val OTHER_USER = MarkdownPage.getPage("account/other/other.md")
    private val ELEVATED_OTHER_USER = MarkdownPage.getPage("account/other/elevated-other.md")

    /**
     * Sort into the different pages.
     */
    override fun getPage(call: ApplicationCall): String {
        val username = call.parameters["user"]

        when {
            username.isNullOrBlank() ->
                return Error(404, "Check for issues in the username.").getPage(call)

            username.equals("self", true) -> {
                if (call.getSession() != null)
                    return getSignedIn(call)

                return SIGNED_OUT modify UrlUtils.FORMAT
            }

            else -> return getOtherUser(call, username)
        }
    }

    /**
     * Get [name]'s account page.
     */
    private fun getOtherUser(call: ApplicationCall, name: String): String {
        val user = UserManager.getUser(name)
                ?: return Error(404, "Check for issues in the username \"$name\".").getPage(call)

        return if (user.permissions.contains(Permission.MOJOR_ADMIN)) {
            OTHER_USER
                    .replace("\$\$NAME", user.username)
        } else {
            ELEVATED_OTHER_USER
                    .replace("\$\$NAME", "<p style=\"color: red;\">${user.username}</p>")
        }
    }

    /**
     * Get a user's permission messages.
     */
    private fun getPermissions(user: User): String =
            buildString {
                user.permissions.forEach { permission ->
                    when (permission) {
                        Permission.MOJOR_ADMIN -> append("You have the elevated dashboard. <br/>")
                        Permission.BUTA_MANAGER -> append("You have permission to manage Buta. <br/>")
                        Permission.USER_MANAGER -> append("You have permission to manage Users. <br/>")
                        Permission.MOTD_MANAGER -> append("You have permission to manage MOTDS. You can do this [here](${UrlUtils.URLS.main}/motd/update).")
                    }
                }
            }

    /**
     * Get the signed in page for someone using [call].
     */
    private fun getSignedIn(call: ApplicationCall): String {
        val ses = call.getSession() ?: throw Exception()

        val token = TokenHandler.getCachedToken(ses.tokenIdentifier)

        if (token == null) {
            call.sessions.clear<Session>()
            return SIGNED_OUT
        } else {
            val owner = UserManager.getUser(token.owner)
                    ?: return SIGNED_OUT // shouldn't ever happen

            val page = if (owner.permissions.contains(Permission.MOJOR_ADMIN)) ELEVATED_SIGN_IN else SIGNED_IN

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