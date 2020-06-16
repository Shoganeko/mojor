package dev.shog.mojor.api.blog.response

import dev.shog.mojor.api.blog.Blog
import dev.shog.mojor.handle.auth.user.obj.User

/**
 * A blog response.
 *
 */
data class BlogResponse(
        val blog: Blog,
        val owner: User
)