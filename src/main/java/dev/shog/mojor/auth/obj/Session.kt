package dev.shog.mojor.auth.obj

data class Session(val tokenIdentifier: String, val signInDate: Long, val signInIp: String)