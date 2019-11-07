package dev.shog.mojor

import dev.shog.mojor.Mojor.LOGGER
import org.apache.commons.lang3.SystemUtils
import org.json.JSONObject
import java.io.File
import kotlin.system.exitProcess

object FileManager {
    /**
     * The directory where the configuration file is stored.
     */
    private val MOJOR_DIR = File(when {
        SystemUtils.IS_OS_WINDOWS -> "${System.getenv("appdata")}\\mojor"
        SystemUtils.IS_OS_LINUX -> "/etc/mojor"
        else -> {
            LOGGER.error("Invalid OS! Please use Windows 10 or Linux (Ubuntu).")
            exitProcess(-1)
        }
    })

    /**
     * The configuration file.
     */
    private val CFG_FILE = File(MOJOR_DIR.path + File.separator + "cfg.json")

    init {
        if (!MOJOR_DIR.exists() && !MOJOR_DIR.mkdir()) {
            LOGGER.error("There was an issue creating the Mojor folder.")
            exitProcess(-1)
        }

        if (!CFG_FILE.exists()) {
            if (CFG_FILE.createNewFile()) {
                initCfg()
            } else {
                LOGGER.error("There was an issue creating the configuration file.")
                exitProcess(-1)
            }
        }
    }

    /**
     * If [CFG_FILE] is empty, initialize it.
     */
    private fun initCfg() {
        if (CFG_FILE.exists()) {
            val str = String(CFG_FILE.inputStream().readBytes())

            if (str.isBlank()) {
                val stream = CFG_FILE.outputStream().bufferedWriter()

                val defaultObj = JSONObject()

                defaultObj.put("dburi", "")
                defaultObj.put("dbu", "")
                defaultObj.put("dbp", "")

                stream.write(defaultObj.toString())

                stream.flush()
                stream.close()
            }
        } else {
            LOGGER.error("There was an issue getting the configuration file. (1)")
            exitProcess(-1)
        }
    }

    /**
     * The JSON from the file.
     */
    private val variables = if (CFG_FILE.exists()) {
        val str = String(CFG_FILE.inputStream().readBytes())

        try {
            JSONObject(str)
        } catch (ex: Exception) {
            LOGGER.error("The configuration file is incorrectly formatted!")
            exitProcess(-1)
        }
    } else {
        LOGGER.error("There was an issue getting the configuration file. (2)")
        exitProcess(-1)
    }

    /**
     * Gets a key from the variables.
     */
    fun get(key: String): Any? {
        val get = variables[key]

        return if (get == "")
            null
        else get
    }
}