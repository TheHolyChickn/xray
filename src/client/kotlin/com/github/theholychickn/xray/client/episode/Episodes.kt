package com.github.theholychickn.xray.client.episode

import com.github.theholychickn.xray.HyperionMod


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
            ep.registerKeybinds()
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

    /** All registered episodes in registration order. */
    fun all(): Collection<Episode> = registry.values
}