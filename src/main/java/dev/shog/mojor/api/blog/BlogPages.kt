package dev.shog.mojor.api.blog

import dev.shog.mojor.api.blog.response.BlogResponse
import dev.shog.mojor.api.response.Response
import dev.shog.mojor.getUuid
import dev.shog.mojor.handle.InvalidArguments
import dev.shog.mojor.handle.auth.isAuthorized
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.auth.user.handle.UserManager
import io.ktor.application.call
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.*
import org.json.JSONArray
import java.util.*

private val cachedResponses = mutableListOf<BlogResponse>()
private val cachedEmoteResponses = mutableListOf<BlogResponse>()

/**
 * Management for blogs.
 */
fun Routing.blogPages() {
    route("/blogs") {
        /**
         * Get all blogs.
         */
        get {
            val params = call.request.queryParameters

            val includeEmotes = params["emotes"]

            when {
                includeEmotes == "true" && cachedEmoteResponses.isNotEmpty() ->
                    call.respond(cachedEmoteResponses)

                includeEmotes == "true" -> {
                    val resp = BlogHandler.getBlogs().map { blog ->
                        val emote = EmoteBlog.fromBlog(blog)

                        BlogResponse(emote, UserManager.getUser(blog.creator))
                    }

                    cachedEmoteResponses.addAll(resp)

                    call.respond(resp)
                }

                cachedResponses.isNotEmpty() ->
                    call.respond(cachedResponses)

                else -> {
                    val resp = BlogHandler.getBlogs().map { blog -> BlogResponse(blog, UserManager.getUser(blog.creator)) }

                    cachedResponses.addAll(resp)

                    call.respond(resp)
                }
            }
        }

        /**
         * Manage an existing [Blog].
         */
        route("/{id}") {
            /**
             * Manage a [Blog]'s tags.
             */
            route("/tags") {
                /**
                 * Delete a tag from an existing blog.
                 */
                delete {
                    call.isAuthorized(Permission.MOJOR_ADMIN)

                    val id = getUuid(call.parameters["id"]) ?: throw InvalidArguments("p_id")

                    val params = call.receiveParameters()
                    val tagName = params["name"] ?: throw InvalidArguments("name")

                    BlogHandler.removeTag(id, tagName)

                    call.respond(Response("Successfully removed tag from $id"))
                }

                /**
                 * Add a tag to an existing blog.
                 */
                post {
                    call.isAuthorized(Permission.MOJOR_ADMIN)

                    val id = getUuid(call.parameters["id"]) ?: throw InvalidArguments("p_id")

                    val params = call.receiveParameters()
                    val tagName = params["name"] ?: throw InvalidArguments("name")

                    BlogHandler.addTag(id, tagName)

                    call.respond(Response("Successfully added tag to $id"))
                }
            }

            /**
             * Get an existing blog.
             */
            get {
                val id = getUuid(call.parameters["id"])
                        ?: throw InvalidArguments("p_id")

                val includeEmotes = call.request.queryParameters["emotes"]
                val blog = BlogHandler.getBlogById(id)
                val owner = UserManager.getUser(blog.creator)

                if (includeEmotes == "true")
                    call.respond(BlogResponse(EmoteBlog.fromBlog(blog), owner))
                else
                    call.respond(BlogResponse(blog, owner))
            }

            /**
             * Delete an existing blog.
             */
            delete {
                call.isAuthorized(Permission.MOJOR_ADMIN)

                val id = getUuid(call.parameters["id"])
                        ?: throw InvalidArguments("p_id")

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
            val tags = JSONArray(params["tags"] ?: "[]")

            if (title == null || body == null)
                throw InvalidArguments("title", "body")

            call.respond(BlogHandler.createBlog(token.owner, title, body, tags))
        }
    }
}