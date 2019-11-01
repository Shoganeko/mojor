package dev.shog.mojor.api.app

import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveParameters
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import org.json.JSONObject

/**
 * Set the app pages.
 */
fun Routing.setAppPages() {
    // Get the page's statistics
    get("/apps/{app}") {
        val app = call.parameters["app"]

        if (app == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        if (!AppAuth.VALID_APPS.containsKey(app.toLowerCase())) {
            call.respond(HttpStatusCode.BadRequest)
            return@get
        }

        val uploadedApp = AppAuth.getUploadedApp(app)

        if (uploadedApp == null) {
            call.respond(HttpStatusCode.BadRequest)
        } else call.respond(uploadedApp.statistics.toString())
    }

    // Update the page's status
    post("/apps/{app}") {
        val app = call.parameters["app"]

        if (app == null) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        if (!AppAuth.VALID_APPS.containsKey(app.toLowerCase())) {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val params = call.receiveParameters()

        if (params.contains("version") && params.contains("stats")) {
            val version = params["version"].toString().toFloatOrNull()

            val stats = try {
                JSONObject(params["stats"])
            } catch (e: Exception) {
                null
            }

            if (version == null || stats == null) {
                call.respond(HttpStatusCode.BadRequest)
                return@post
            }

            val uploadedApp = UploadedApp(app, version, stats, System.currentTimeMillis())

            if (AppAuth.uploadApp(uploadedApp)) {
                call.respond(HttpStatusCode.OK)
            } else {
                call.respond(HttpStatusCode.BadRequest)
            }
            return@post
        }

        call.respond(HttpStatusCode.BadRequest)
    }
}