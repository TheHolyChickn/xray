package com.github.theholychickn.xray

import com.github.theholychickn.xray.entities.HyperionAction
import com.github.theholychickn.xray.entities.HyperionCommands
import com.github.theholychickn.xray.entities.HyperionEntity
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricDefaultAttributeRegistry
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.MobCategory
import org.slf4j.LoggerFactory

class HyperionMod : ModInitializer {

    companion object {
        val MOD_ID = "not-enough-racism"
        val LOGGER = LoggerFactory.getLogger(MOD_ID)
        fun id(path: String): Identifier = Identifier.fromNamespaceAndPath(MOD_ID, path)

        private val HYPERION_ENTITY_KEY: ResourceKey<EntityType<*>> =
            ResourceKey.create(Registries.ENTITY_TYPE, id("hyperion"))

        val HYPERION_ENTITY: EntityType<HyperionEntity> = EntityType.Builder
            .of(::HyperionEntity, MobCategory.MISC)
            .sized(0.6f, 1.8f)
            .eyeHeight(1.62f)
            .build(HYPERION_ENTITY_KEY)
    }

    override fun onInitialize() {
        @Suppress("UNCHECKED_CAST")
        Registry.register(BuiltInRegistries.ENTITY_TYPE, id("hyperion"), HYPERION_ENTITY)
        FabricDefaultAttributeRegistry.register(HYPERION_ENTITY, HyperionEntity.createAttributes().build())

        CommandRegistrationCallback.EVENT.register { dispatcher, buildCtx, _ ->
            HyperionCommands.register(dispatcher, buildCtx)
        }

        // register the tickscheduler for handling hyperion action strings
        ServerTickEvents.END_SERVER_TICK.register { _ -> TickScheduler.tick() }

        LOGGER.info("Awake.")
    }
}