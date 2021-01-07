package dev.shog.mojor.api.blog.response

import dev.shog.mojor.api.blog.Blog
import dev.shog.mojor.api.users.obj.User

/**
 * A response to getting a blog.
 */
data class BlogResponse(
    val blog: Blog,
    val user: User
)