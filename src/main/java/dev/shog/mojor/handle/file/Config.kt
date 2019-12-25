package dev.shog.mojor.handle.file

import dev.shog.mojor.Mojor

/**
 * The configuration file for Mojor.
 */
class Config {
    /** The version this configuration file was intended for */
    var intendedVersion: Float = Mojor.VERSION

    /** The PostgreSQL configuration */
    var postgre = DatabaseCredentials()

    /** Credentials for Postgre */
    class DatabaseCredentials {
        var username: String = ""
        var password: String = ""
        var url: String = ""
    }

    /** The secret for the Captcha */
    var captchaSecret: String = ""

    /** The URL for the Discord web-hook */
    var discordUrl: String = ""

    companion object {
        /**
         * The instance of the config, filled in with the proper values.
         */
        lateinit var INSTANCE: Config
    }
}