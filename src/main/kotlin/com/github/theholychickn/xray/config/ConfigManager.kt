package com.github.theholychickn.xray.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File

data class HyperionConfig(
    var activeEpisode: String = "apollyon_upload_1"
)

object ConfigManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(
        FabricLoader.getInstance().configDir.toFile(),
        "not-enough-racism.json"
    )

    var config = HyperionConfig()
        private set

    fun load() {
        if (file.exists()) {
            runCatching {
                config = gson.fromJson(file.readText(), HyperionConfig::class.java)
            }.onFailure { ex ->
                ex.printStackTrace()
                // corrupted config, reset to defaults and overwrite
                config = HyperionConfig()
                save()
            }
        } else {
            save()
        }
    }

    fun save() {
        file.writeText(gson.toJson(config))
    }
}
