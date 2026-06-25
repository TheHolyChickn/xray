package com.github.theholychickn.xray.client

import com.github.theholychickn.xray.HyperionMod
import com.github.theholychickn.xray.client.entities.HyperionRenderer
import com.github.theholychickn.xray.client.episode.ApollyonUpload1
import com.github.theholychickn.xray.client.episode.Episode
import com.github.theholychickn.xray.client.episode.Episodes
import com.github.theholychickn.xray.config.ConfigManager
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements
import net.minecraft.client.KeyMapping
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier

class HyperionModClient : ClientModInitializer {

    companion object {
        /** Shared keybind category. Episodes reference this from their [Episode.keybinds] property
         * initializers, which are evaluated during [onInitializeClient] after this object
         * has already been initialized.
         */
        val category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath(HyperionMod.MOD_ID, "main")
        )
    }

    private var snapshotCooldown = -1

    override fun onInitializeClient() {
        ConfigManager.load()

        // entity renderer
        EntityRendererRegistry.register(HyperionMod.HYPERION_ENTITY) { context ->
            HyperionRenderer(context)
        }

        // ── Register all episodes (also registers their keybinds with Fabric) ─────────
        // Add new episodes here as the ARG grows.
        Episodes.register(
            ApollyonUpload1()
        )

        // ── Activate the episode specified in the config file ─────────────────────────
        Episodes.activate(ConfigManager.config.activeEpisode)

        registerHud()
        registerWorldEvents()
        registerKeybinds()
    }

    // ── HUD ───────────────────────────────────────────────────────────────────────────

    /**
     * Attaches [ThoughtGui] as a HUD layer rendered just before the chat overlay.
     * This ensures the thought log sits below chat messages in the layer stack.
     */
    private fun registerHud() {
        HudElementRegistry.attachElementBefore(
            VanillaHudElements.CHAT,
            Identifier.fromNamespaceAndPath(HyperionMod.MOD_ID, "thought_gui")
        ) { graphics, deltaTracker ->
            ThoughtGui.render(graphics, deltaTracker)
        }
    }

    private fun registerWorldEvents() {
        ClientPlayConnectionEvents.JOIN.register { _, _, _ ->
            snapshotCooldown = 60
        }
    }

    // ── keybinds ──────────────────────────────────────────────────────────────────────

    /**
     * Registers a key mapping (default: H) that toggles [ThoughtGui] visibility
     * and immediately persists the change to the config file.
     *
     * The key is intentionally set to H (for Hyperion) rather than a common key
     * so it doesn't interfere with normal gameplay during recording.
     */
    private fun registerKeybinds() {
        ClientTickEvents.END_CLIENT_TICK.register { client ->
            if (snapshotCooldown > 0) {
                if (--snapshotCooldown == 0) {
                    Episodes.snapshotAllWorldStates()
                }
            }

            val ep = Episodes.active ?: return@register
            ep.keybinds.forEach { (key, mapping) ->
                while (key.consumeClick()) { mapping.invoke(client) }
            }
        }
    }
}