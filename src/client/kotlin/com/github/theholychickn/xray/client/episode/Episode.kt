package com.github.theholychickn.xray.client.episode

import com.github.theholychickn.xray.client.dialog.ThoughtEntry
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft


/**
 * Base class for all episodes.
 *
 * @param id                   Config-file identifier for this episode.
 * @param shouldRenderThoughts Whether [ThoughtGui] should render while this episode is active.
 * @param thoughtLog           Thought log entries for this episode. Empty = nothing shown.
 *
 * ## Keybinds
 * Override [keybinds] as a `val` property (not a constructor param) so the lambda bodies
 * can reference `this` safely — property initializers in a subclass run after `super()`
 * but before any code can access the instance externally, so by the time a key is pressed
 * and the lambda fires, construction is complete.
 *
 * ```kt
 * override val keybinds = mapOf(
 *     KeyMapping("key.mod.reset", ..., HyperionModClient.category) to { _ -> reset() }
 * )
 * ```
 *
 * [Episodes.register] calls [registerKeybinds] once per episode after construction,
 * so [KeyMappingHelper.registerKeyMapping] runs during client init, not lazily.
 */

abstract class Episode(
    val id: String,
    val shouldRenderThoughts: Boolean = false,
    val thoughtLog: List<ThoughtEntry> = listOf()
) {
    /**
     * Map of [KeyMapping] → action. Override in subclasses.
     * [HyperionModClient] ticks the *active* episode's bindings only,
     * so keys from inactive episodes will not fire.
     */
    open val keybinds: Map<KeyMapping, (Minecraft) -> Unit> = emptyMap()

    /** Resets this episode to its starting state. Called on activation and by reset keybinds. */
    abstract fun reset()

    /** Called by [Episodes.register] — do not call manually. */
    internal fun registerKeybinds() {
        keybinds.keys.forEach { KeyMappingHelper.registerKeyMapping(it) }
    }
}