package com.example

import net.fabricmc.api.ModInitializer
import net.minecraft.resources.Identifier
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object ExampleMod : ModInitializer {
    const val MOD_ID = "not-enough-racism"
    val LOGGER: Logger = LoggerFactory.getLogger(MOD_ID)

    override fun onInitialize() {
        LOGGER.info("Hello Fabric world!")
    }

    fun id(path: String): Identifier {
        return Identifier.fromNamespaceAndPath(MOD_ID, path)
    }
}