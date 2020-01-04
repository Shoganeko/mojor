package dev.shog.mojor.api.buta

import dev.shog.mojor.handle.db.PostgreSql
import org.json.JSONArray
import org.json.JSONObject

/**
 * The presences page.
 */
object ButaPresencesPage {
    private var content: JSONArray? = null

    /**
     * Refresh [content].
     */
    fun refresh() {
        val rs = PostgreSql.createConnection()
                ?.prepareStatement("SELECT * FROM buta.presences")
                ?.executeQuery()

        val array = JSONArray()
        while (rs?.next() == true) {
            array.put(JSONObject()
                    .put("statusType", rs.getInt("statusType"))
                    .put("activityType", rs.getInt("activityType"))
                    .put("statusText", rs.getString("statusText")))
        }

        content = array
    }

    /**
     * Get the page.
     */
    fun getPage(): JSONArray {
        if (content == null) {
            refresh()
        }

        return content ?: JSONArray()
    }
}