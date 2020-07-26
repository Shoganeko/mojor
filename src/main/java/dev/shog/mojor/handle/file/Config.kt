package dev.shog.mojor.handle.file

import dev.shog.mojor.Mojor

/**
 * The configuration file for Mojor.
 */
class Config {

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

    /**
     * The discord app secret
     */
    var discordSecret: String = ""

    /**
     * The discord client ID
     */
    var discordId: String = ""

    /**
     * The Mongo DB password.
     */
    var mongoPass: String = ""
}