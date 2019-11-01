package dev.shog.def.auth

import dev.shog.def.App
import dev.shog.def.auth.objs.IAuthData
import dev.shog.def.auth.objs.IAuthDevice
import io.ktor.client.HttpClient
import io.ktor.client.request.forms.MultiPartFormDataContent
import io.ktor.client.request.forms.formData
import io.ktor.client.request.request
import io.ktor.http.HttpMethod
import org.apache.commons.codec.digest.DigestUtils
import org.json.JSONObject

/**
 * The default auth device.
 */
class SimpleAuthDevice(private val url: String, private val app: App) : IAuthDevice {
    override suspend fun authenticate(data: IAuthData): Boolean {
        require(data is SimpleAuthData)

        try {
            val resp = HttpClient().request<String>(url) {
                method = HttpMethod.Post
                body = MultiPartFormDataContent(formData {
                    append("username", data.username)
                    append("password", DigestUtils.sha512Hex(data.password))
                })
            }

            val json = JSONObject(resp)

            if (json.has("data")) {
                val jsObj = json.getJSONObject("data")

                if (jsObj.has("token"))
                    app.token = jsObj.getString("token")
            }

            return false
        } catch (ex: Exception) {
            return false
        }

    }
}