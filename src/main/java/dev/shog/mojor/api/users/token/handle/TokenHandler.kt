package dev.shog.mojor.api.users.token.handle

import com.mongodb.client.model.Filters
import dev.shog.mojor.api.users.obj.User
import dev.shog.mojor.api.users.token.obj.Token
import dev.shog.mojor.handle.InvalidAuthorization
import dev.shog.mojor.handle.auth.obj.Permission
import dev.shog.mojor.handle.db.Mongo
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.commons.codec.digest.DigestUtils
import org.bson.Document
import java.security.SecureRandom
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask

/**
 * Manages tokens.
 */
object TokenHandler {
    const val EXPIRE_AFTER = 1000L * 60L * 24L * 24L * 7L // A week.

    init {
        Timer().schedule(timerTask {
            runBlocking { clearExpired() }
        }, 0, TimeUnit.HOURS.toMillis(1))
    }

    /**
     * The cache of token. This initializes by getting all tokens from the database.
     */
    private val cache: MutableList<Token> by lazy {
        fun parsePermissions(permissions: List<String>): Collection<Permission> =
                permissions.map { permission -> Permission.valueOf(permission) }

        Mongo.getClient()
                .getDatabase("users")
                .getCollection("tokens")
                .find()
                .map { doc ->
                    Token(
                            doc.getString("token"),
                            UUID.fromString(doc.getString("owner")),
                            parsePermissions(doc["permissions"] as List<String>),
                            doc.getLong("createdon")
                    )
                }
                .toMutableList()
    }

    /**
     * Get all of the expired tokens from [TOKEN_CACHE] and remove them.
     *
     * TODO use deletemany
     */
    private suspend fun clearExpired() {
        val expired = cache.filter { token -> isTokenExpired(token) }

        removeTokens(expired)
    }

    /**
     * Remove [token] from [TOKEN_CACHE] and the DB.
     */
    suspend fun removeTokens(tokens: List<Token>) {
        coroutineScope {
            val collection = Mongo.getClient()
                    .getDatabase("users")
                    .getCollection("users")
            tokens.forEach { token ->
                cache.removeIf { cacheToken -> cacheToken.token == token.token }

                launch {
                    collection.deleteOne(Filters.eq("token", token.token))
                }
            }
        }
    }

    /**
     * If [token] is expired.
     * It is expired if the token's creation time minus the current time is greater than or equal to [EXPIRE_AFTER].
     */
    fun isTokenExpired(token: Token): Boolean =
            System.currentTimeMillis() - token.createdOn >= EXPIRE_AFTER

    /**
     * Get a token by it's [token]/
     */
    fun getToken(token: String): Token =
            cache.singleOrNull { cacheToken -> cacheToken.token == token }
                    ?: throw InvalidAuthorization("invalid token")

    /**
     * The secure random used for token string generation.
     */
    private val SEC_RAND = SecureRandom()

    /**
     * Create an unused token identifier.
     */
    private suspend fun getTokenIdentifier(): String {
        val bytes = ByteArray(64)
        SEC_RAND.nextBytes(bytes)
        val token = DigestUtils.sha256Hex(String(bytes))

        val exists = Mongo.getClient()
                .getDatabase("users")
                .getCollection("tokens")
                .find(Filters.eq("token", token))
                .any()

        return if (exists) getTokenIdentifier() else token
    }

    /**
     * Create a unique [Token] with [user] as the owner.
     */
    suspend fun createToken(user: User): Token {
        val identifier = getTokenIdentifier()
        val token = Token(identifier, user.id, user.permissions, System.currentTimeMillis())

        cache.add(token)

        Mongo.getClient()
                .getDatabase("users")
                .getCollection("tokens")
                .insertOne(Document(mapOf(
                        "token" to token.token,
                        "owner" to token.owner.toString(),
                        "createdon" to token.createdOn,
                        "permissions" to token.permissions.map(Permission::toString)
                )))

        return token
    }
}

