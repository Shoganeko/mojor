package dev.shog.mojor.file

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import dev.shog.mojor.Mojor
import dev.shog.mojor.Mojor.LOGGER
import org.apache.commons.lang3.SystemUtils
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
            LOGGER.error("Invalid OS! Please use Windows (10) or Linux (Ubuntu).")
            exitProcess(2)
        }
    })

    /**
     * The configuration file.
     */
    private val CFG_FILE = File(MOJOR_DIR.path + File.separator + "config.yml")

    init {
        if (!MOJOR_DIR.exists() && !MOJOR_DIR.mkdir())
            exitProcess(3)

        if (!CFG_FILE.exists()) {
            if (CFG_FILE.createNewFile()) {
                initCfg()
            } else exitProcess(3)
        }

        val fileConfig = getConfigFromFile()

        if (fileConfig.intendedVersion != Mojor.VERSION)
            exitProcess(6)

        Config.INSTANCE = fileConfig
    }

    /**
     * If [CFG_FILE] is empty, initialize it.
     */
    private fun initCfg() {
        if (CFG_FILE.exists()) {
            val str = String(CFG_FILE.inputStream().readBytes())

            if (str.isBlank()) {
                val stream = CFG_FILE.outputStream().bufferedWriter()

                val configInstance = Config()

                Config.INSTANCE = configInstance

                stream.write(ObjectMapper(YAMLFactory()).writeValueAsString(configInstance))

                stream.flush()
                stream.close()
            }
        } else exitProcess(4)
    }

    /**
     * Get a [Config] from the [CFG_FILE].
     */
    private fun getConfigFromFile(): Config {
        val fileBytes = CFG_FILE.readBytes()

        return try {
            ObjectMapper(YAMLFactory()).readValue(fileBytes, Config::class.java)
        } catch (ex: Exception) {
            exitProcess(5)
        }
    }
}