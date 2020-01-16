package dev.shog.mojor.handle.file

import dev.shog.mojor.Mojor

/**
 * The configuration file for Mojor.
 */
class Config {
    /**
     * The version this configuration file was intended for
     */
    var version: Float = Mojor.MOJOR_VERSION

    /**
     * The PostgreSQL configuration
     */
    var postgre = DatabaseCredentials()

    /** Credentials for Postgre */
    class DatabaseCredentials {
        var username: String = ""
        var password: String = ""
        var url: String = ""
    }

    /**
     * The secret for the Captcha
     */
    var captchaSecret: String = ""

    /**
     * The URL for the Discord web-hook
     */
    var discordUrl: String = ""
}