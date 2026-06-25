package com.github.theholychickn.xray.client.episode

import com.github.theholychickn.xray.HyperionMod
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft


/**
 * Registry and activator for all [Episode]s.
 *
 * Usage in [HyperionModClient.onInitializeClient]:
 * ```kt
 * Episodes.register(ApollyonUpload1())
 * Episodes.activate(ConfigManager.config.activeEpisode)
 * ```
 */
object Episodes {

    private val registry = linkedMapOf<String, Episode>()

    private val computedKeybinds = mutableMapOf<String, Map<KeyMapping, (Minecraft) -> Unit>>()

    /** The currently active episode, or null if none are active. */
    var active: Episode? = null
        private set

    /**
     * Registers one or more episodes and calls [Episode.registerKeybinds] on each.
     * Must be called during [ClientModInitializer.onInitializeClient]
     */
    fun register(vararg episodes: Episode) {
        for (ep in episodes) {
            registry[ep.id] = ep
            val generated = ep.buildGeneratedKeybinds()
            computedKeybinds[ep.id] = ep.keybinds + generated
            ep.registerKeybinds(generated)
        }
    }

    /**
     * Activates the episode with the given [id], calling [Episode.reset] on it.
     * If [id] is not found, falls back to the first registered episode and logs a warning.
     * Returns the activated episode, or null if the registry is empty.
     */
    fun activate(id: String): Episode? {
        val ep = registry[id] ?: run {
            HyperionMod.LOGGER.warn(
                "[Hyperion] No episode registered with id '$id'." +
                "Defaulting to '${registry.keys.firstOrNull()}'."
            )
            registry.values.first()
        }
        active = ep
        ep.reset()
        return ep
    }

    // ── queries ───────────────────────────────────────────────────────────────────────

    /** All keybinds (manual + generated) for the active episode. */
    fun getActiveKeybinds(): Map<KeyMapping, (Minecraft) -> Unit> =
        computedKeybinds[active?.id] ?: emptyMap()

    /** All registered episodes, in registration order. */
    fun all(): Collection<Episode> = registry.values

    // ── world events ──────────────────────────────────────────────────────────────────

    /**
     * Snapshots block states for every registered episode.
     * Called by [HyperionModClient] a few ticks after a world is joined,
     * once chunks around the player are loaded.
     */
    fun snapshotAllWorldStates() {
        registry.values.forEach { it.snapshotWorld() }
    }
}