package dev.shog.mojor.auth.user

import dev.shog.mojor.handle.db.PostgreSql
import reactor.core.publisher.Mono
import reactor.kotlin.core.publisher.toMono
import kotlin.random.Random

/**
 * Generates an unused user ID.
 * This ID is a [Long] that is 18 characters long.
 */
class UserIdGenerator {
    /** The generated ID. */
    private var generatedId: Mono<Long> = Mono.empty()

    /** Create a 18 character long user ID */
    private fun createId(): Long {
        var preId = ""
        (0..17).forEach { _ ->
            preId += Random.nextInt(10)
        }

        return preId.toLong()
    }

    /** Update [generatedId] with a possibly valid ID */
    private fun update(): UserIdGenerator {
        generatedId = createId()
                .toMono()
                .filterWhen { id -> checkExists(id) }

        return this
    }

    /** Check if [id] is an already used ID */
    private fun checkExists(id: Long): Mono<Boolean> =
            PostgreSql.monoConnection()
                    .map { sql -> sql.prepareStatement("SELECT name FROM users.users WHERE ID = ?") }
                    .doOnNext { pre -> pre.setLong(1, id) }
                    .map { pre -> pre.executeQuery() }
                    .map { rs -> rs.fetchSize == 0 }

    /** Retrieve [generatedId] */
    fun retrieve(): Mono<Long> =
            generatedId

    /** Manages the ability to retry a request to a [UserIdGenerator] */
    private class UserIdRetryEngine(val userIdGenerator: UserIdGenerator) {
        /** If this exceeds 10, an exception will be thrown */
        private var tries = 0

        /** Retry and count using [tries]. */
        private fun retry(): Mono<Long> {
            if (tries >= 10)
                throw Exception("An attempt to retry creation has been done 10 or more times!")

            tries++

            return userIdGenerator
                    .update()
                    .retrieve()
        }

        /** Get an used ID. */
        fun getDefiniteNonEmpty(): Mono<Long> =
                userIdGenerator
                        .update()
                        .retrieve()
                        .repeatWhenEmpty { retry() }
    }

    companion object {
        /** Get an ID that is unused */
        fun getId(): Mono<Long> =
                UserIdRetryEngine(UserIdGenerator())
                        .getDefiniteNonEmpty()
    }
}