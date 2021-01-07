package dev.shog.mojor.api.buta.api

data class User(
    val id: Long,
    val username: String,
    val discriminator: String,
    val avatar: String?
)