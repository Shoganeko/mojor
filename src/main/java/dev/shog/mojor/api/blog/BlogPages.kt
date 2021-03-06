package dev.shog.mojor.api.blog

import dev.shog.mojor.api.blog.response.BlogResponse
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.api.users.handle.UserManager
import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*
import org.json.JSONArray

/**
 * Management for blogs.
 */
fun Routing.blogPages() {
    route("/blogs") {
        /**
         * Get all blogs.
         */
        get {
            call.respond(BlogHandler.getBlogs().map { blog ->
                BlogResponse(blog, UserManager.getUser(blog.creator))
            })
        }

        /**
         * Manage an existing [Blog].
         */
        route("/{id}") {
            /**
             * Get an existing blog.
             */
            get {
                val id = getUuid(call.parameters["id"])

                val blog = BlogHandler.getBlogById(id)
                val owner = UserManager.getUser(blog.creator)

                call.respond(BlogResponse(blog, owner))
            }

            /**
             * Delete an existing blog.
             */
            delete {
                call.isAuthorized(Permission.MOJOR_ADMIN)

                val id = getUuid(call.parameters["id"])

                BlogHandler.deleteBlog(id)

                call.respond(Response())
            }
        }

        /**
         * Create a blog.
         */
        post {
            val token = call.isAuthorized(Permission.MOJOR_ADMIN)

            val params = call.receiveParameters()

            val title = params["title"]
            val body = params["body"]
            val tags = JSONArray(params["tags"] ?: "[]").toList() as? List<String>

            if (title == null || body == null || tags == null)
                throw InvalidArguments("title", "body", "tags")

            call.respond(BlogHandler.createBlog(token.owner, title, body))
        }
    }
}