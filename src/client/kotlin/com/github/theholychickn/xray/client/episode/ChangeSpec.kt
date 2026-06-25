package com.github.theholychickn.xray.client.episode

import com.github.theholychickn.xray.client.BlockModifier
import net.minecraft.core.BlockPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.state.BlockState


/**
 * Describes a block change to be executed when a keybind is pressed.
 *
 * Usage in episode constructor:
 * ```kt
 * blockKeybindMap = mapOf(
 *     GLFW.GLFW_KEY_K to listOf(
 *         ChangeSpec.Single(BlockPos(-41, 103, -169), Blocks.AIR),
 *         ChangeSpec.Region(BlockPos(-10, 64, -10), BlockPos(10, 64, 10), Blocks.STONE)
 *     )
 * )
 * ```
 */
sealed class ChangeSpec {

    /** Executes this change via [com.github.theholychickn.xray.client.BlockModifier] */
    abstract fun apply()

    /** All block positions affected by this change. Used for snapshotting initial state. */
    abstract fun positions(): List<BlockPos>

    // ── single block ──────────────────────────────────────────────────────────────────

    data class Single(val pos: BlockPos, val state: BlockState) : ChangeSpec() {
        /** Convenience constructor that uses a block's default state. */
        constructor(pos: BlockPos, block: Block) : this(pos, block.defaultBlockState())

        override fun apply() = BlockModifier.set(pos, state)
        override fun positions() = listOf(pos)
    }

    // ── rectangular region ────────────────────────────────────────────────────────────

    data class Region(val c1: BlockPos, val c2: BlockPos, val state: BlockState) : ChangeSpec() {
        /** Convenience constructor that uses a block's default state. */
        constructor(c1: BlockPos, c2: BlockPos, block: Block) : this(c1, c2, block.defaultBlockState())

        override fun apply() = BlockModifier.setRegion(c1, c2, state)

        override fun positions(): List<BlockPos> =
            BlockPos.betweenClosed(c1, c2).map { it.immutable() }
    }
}
