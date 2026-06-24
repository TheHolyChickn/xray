package com.github.theholychickn.xray.config

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import net.fabricmc.loader.api.FabricLoader
import java.io.File

data class XRayConfig(
    var distance: Int = 4,
    var targetBlocks: List<String> = listOf("minecraft:diamond_ore", "minecraft:deepslate_diamond_ore", "minecraft:ancient_debris")
)

object ConfigManager {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
    private val file = File(FabricLoader.getInstance().configDir.toFile(), "not-enough-racism.json")
    var config = XRayConfig()

    fun load() {
        if (file.exists()) {
            try {
                config = gson.fromJson(file.readText(), XRayConfig::class.java)
                config.distance = config.distance.coerceIn(1, 15)
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
        } else {
            save()
        }
    }

    fun save() {
        file.writeText(gson.toJson(config))
    }
}
