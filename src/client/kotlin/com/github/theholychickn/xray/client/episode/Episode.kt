package com.github.theholychickn.xray.client.episode

import com.github.theholychickn.xray.client.BlockModifier
import com.github.theholychickn.xray.client.HyperionModClient
import com.github.theholychickn.xray.client.dialog.ThoughtEntry
import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.state.BlockState


/**
 * Base class for all episodes.
 *
 * @param id                   Config-file identifier for this episode.
 * @param shouldRenderThoughts Whether [ThoughtGui] should render while this episode is active.
 * @param thoughtLog           Thought log entries for this episode. Empty = nothing shown.
 * @param blockKeybindMap      Optional: maps GLFW key codes to lists of [ChangeSpec]s.
 *                             Each entry auto-generates a keybind that applies all its
 *                             changes when pressed. Initial states are snapshotted on world
 *                             join so [restoreBlockStates] can be called in [reset].
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
    val thoughtLog: List<ThoughtEntry> = listOf(),
    val blockKeybindMap: Map<Int, List<ChangeSpec>> = emptyMap()
) {
    /**
     * Map of [KeyMapping] → action. Override in subclasses.
     * [HyperionModClient] ticks the *active* episode's bindings only,
     * so keys from inactive episodes will not fire.
     */
    open val keybinds: Map<KeyMapping, (Minecraft) -> Unit> = emptyMap()

    /** Block states saved by [snapshotWorld], restored by [restoreBlockStates]. */
    private val savedBlockStates = mutableMapOf<BlockPos, BlockState>()

    // ── block state snapshot / restore ────────────────────────────────────────────────

    /**
     * Reads and caches the current state of every position referenced by [blockKeybindMap].
     * Called by [Episodes] when a world is joined (after a short tick delay to allow
     * chunks to load). Call again manually if you need a fresh snapshot.
     */
    fun snapshotWorld() {
        savedBlockStates.clear()
        blockKeybindMap.values
            .flatten()
            .flatMap { it.positions() }
            .distinct()
            .forEach { pos -> savedBlockStates[pos] = BlockModifier.getState(pos) }
    }

    /**
     * Restores all tracked positions to their snapshotted states.
     * Call this from [reset] in subclasses that use [blockKeybindMap].
     */
    protected fun restoreBlockStates() {
        savedBlockStates.forEach { (pos, state) -> BlockModifier.set(pos, state) }
    }

    // ── abstract ──────────────────────────────────────────────────────────────────────

    /** Resets this episode to its starting state. Called on activation and by reset keybinds. */
    abstract fun reset()

    // ── internal — called by Episodes ─────────────────────────────────────────────────

    /** Builds auto-generated keybinds from [blockKeybindMap]. */
    internal fun buildGeneratedKeybinds(): Map<KeyMapping, (Minecraft) -> Unit> {
        val result = linkedMapOf<KeyMapping, (Minecraft) -> Unit>()
        blockKeybindMap.forEach { (glfwKey, specs) ->
            result[KeyMapping(
                "key.not-enough-racism.$id.blocks.$glfwKey",
                InputConstants.Type.KEYSYM,
                glfwKey,
                HyperionModClient.category
            )] = { _ -> specs.forEach { it.apply() } }
        }
        return result
    }

    /** Called by [Episodes.register] — do not call manually. */
    internal fun registerKeybinds(generated: Map<KeyMapping, (Minecraft) -> Unit>) {
        keybinds.keys.forEach { KeyMappingHelper.registerKeyMapping(it) }
        generated.keys.forEach { KeyMappingHelper.registerKeyMapping(it) }
    }
}