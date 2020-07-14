package dev.shog.mojor.api.buta

import dev.shog.mojor.handle.NotFound
import dev.shog.mojor.handle.db.PostgreSql
import java.sql.ResultSet
import java.util.concurrent.ConcurrentHashMap

object TokenHandler {
    private val tokenCache: ConcurrentHashMap<String, DiscordToken> by lazy {
        val rs = PostgreSql.getConnection("Get buta token by token")
                .prepareStatement("SELECT * FROM buta.tokens")
                .executeQuery()

        val list = ConcurrentHashMap<String, DiscordToken>()

        while (rs.next()) {
            list[rs.getString("id")] = getToken(rs)
        }

        list
    }

    @Throws(NotFound::class)
    fun getToken(token: String): DiscordToken =
            tokenCache[token] ?: throw NotFound("buta_token")

    fun uploadToken(token: DiscordToken) {
        tokenCache[token.id] = token

        PostgreSql.getConnection("Upload buta token")
                .prepareStatement("INSERT INTO buta.tokens (token_type, expires_in, refresh_token, scope, access_token, id) VALUES (?, ?, ?, ?, ?, ?)")
                .apply {
                    setString(1, token.tokenType)
                    setInt(2, token.expiresIn)
                    setString(3, token.refreshToken)
                    setString(4, token.scope)
                    setString(5, token.accessToken)
                    setString(6, token.id)
                }
                .executeUpdate()
    }

    private fun getToken(rs: ResultSet): DiscordToken =
            DiscordToken(
                    rs.getString("token_type"),
                    rs.getInt("expires_in"),
                    rs.getString("refresh_token"),
                    rs.getString("scope"),
                    rs.getString("access_token"),
                    rs.getString("id")
            )

    @Throws(NotFound::class)
    fun deleteToken(token: String) {
        val obj = getToken(token)

        tokenCache.remove(token)

        PostgreSql.getConnection("Delete buta token")
                .prepareStatement("DELETE FROM buta.tokens WHERE id = ?")
                .apply { setString(1, obj.id) }
                .executeUpdate()
    }
}