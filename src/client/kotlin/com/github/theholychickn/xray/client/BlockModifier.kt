package com.github.theholychickn.xray.client

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState


/**
 * Utility for reading and writing blocks in the current world.
 *
 *
 * ## Thread safety
 * [set] and [setRegion] calls dispatche to the integrated server thread via `server.execute { }`, so it is safe
 * to call from the render thread or the client tick thread. [getState] reads from the
 * client-side shadow level and is safe to call from any client thread.
 *
 * ## Multiplayer
 * [set] and [setRegion] require an integrated (singleplayer) server. Calling it while connected to a
 * remote server is a no-op; a warning is logged.
 */
object BlockModifier {

    // ── set ───────────────────────────────────────────────────────────────────────────

    /** Sets the block at ([x], [y], [z]) to the default state of [block]. */
    fun set(x: Int, y: Int, z: Int, block: Block) =
        set(BlockPos(x, y, z), block.defaultBlockState())

    /** Sets the block at ([x], [y], [z]) to [state]. */
    fun set(x: Int, y: Int, z: Int, state: BlockState) =
        set(BlockPos(x, y, z), state)

    /**
     * Sets the block at [pos] to [state].
     *
     * Dispatches to the integrated server thread. The change is replicated to the
     * client automatically via the standard block update flag (`UPDATE_ALL = 3`).
     */
    fun set(pos: BlockPos, state: BlockState) {
        val client = Minecraft.getInstance()
        val server = client.singleplayerServer
        if (server == null) {
            client.player?.sendSystemMessage(
                net.minecraft.network.chat.Component.literal(
                    "[Hyperion] BlockModifier.set() requires a singleplayer world."
                )
            )
            return
        }
        val dimension = client.player?.level()?.dimension() ?: Level.OVERWORLD
        server.execute {
            server.getLevel(dimension)?.setBlock(pos, state, 3)
        }
    }

    // ── setRegion (two-corner fill) ───────────────────────────────────────────────────

    fun setRegion(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int, block: Block) =
        setRegion(BlockPos(x1, y1, z1), BlockPos(x2, y2, z2), block.defaultBlockState())

    fun setRegion(x1: Int, y1: Int, z1: Int, x2: Int, y2: Int, z2: Int, state: BlockState) =
        setRegion(BlockPos(x1, y1, z1), BlockPos(x2, y2, z2), state)

    fun setRegion(c1: BlockPos, c2: BlockPos, block: Block) =
        setRegion(c1, c2, block.defaultBlockState())

    /**
     * Fills the axis-aligned box between [c1] and [c2] (inclusive) with [state].
     * Uses flag `2` (UPDATE_CLIENTS only) on each block to avoid cascading neighbour
     * updates for large fills; the client receives all changes normally.
     */
    fun setRegion(c1: BlockPos, c2: BlockPos, state: BlockState) {
        val client = Minecraft.getInstance()
        val server = client.singleplayerServer ?: return
        val dim    = client.player?.level()?.dimension() ?: Level.OVERWORLD
        server.execute {
            val level = server.getLevel(dim) ?: return@execute
            BlockPos.betweenClosed(c1, c2).forEach { pos ->
                level.setBlock(pos, state, 2)
            }
        }
    }

    // ── getState ──────────────────────────────────────────────────────────────────────

    /** Returns the [BlockState] at ([x], [y], [z]) from the client-side level. */
    fun getState(x: Int, y: Int, z: Int): BlockState = getState(BlockPos(x, y, z))

    /**
     * Returns the [BlockState] at [pos] from the client-side level.
     * Returns [Blocks.AIR]'s default state if no level is loaded.
     */
    fun getState(pos: BlockPos): BlockState =
        Minecraft.getInstance().level?.getBlockState(pos)
            ?: Blocks.AIR.defaultBlockState()

}