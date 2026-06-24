package com.github.theholychickn.xray.client

import com.github.theholychickn.xray.config.ConfigManager
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.chunk.status.ChunkStatus
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors

object BlockScanner {
    val cachedBlocks = ConcurrentHashMap<ChunkPos, List<BlockPos>>()
    private val executor = Executors.newSingleThreadExecutor()
    private var isScanning = false

    fun startScan() {
        if (isScanning) return

        val client = Minecraft.getInstance()
        val player = client.player ?: return
        val level = client.level ?: return

        val distance = ConfigManager.config.distance
        val centerChunk = player.chunkPosition()

        val targets: Set<Block> = ConfigManager.config.targetBlocks.mapNotNull {
            val id = Identifier.tryParse(it) ?: return@mapNotNull null
            BuiltInRegistries.BLOCK.get(id).orElse(null)?.value()
        }.toSet()

        isScanning = true
        executor.submit {
            try {
                val requiredChunks = mutableSetOf<ChunkPos>()
                for (x in -distance..distance) {
                    for (z in -distance..distance) {
                        requiredChunks.add(ChunkPos(centerChunk.x + x, centerChunk.z + z))
                    }
                }

                cachedBlocks.keys.retainAll(requiredChunks)

                for (chunkPos in requiredChunks) {
                    if (cachedBlocks.containsKey(chunkPos)) continue

                    val chunk = level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false)
                    val found = mutableListOf<BlockPos>()

                    val startX = chunkPos.minBlockX
                    val startZ = chunkPos.minBlockZ

                    for (x in 0..15) {
                        for (z in 0..15) {
                            for (y in level.minY until level.maxY) {
                                val pos = BlockPos(startX + x, y, startZ + z)
                                val state = chunk?.getBlockState(pos) ?: continue
                                if (targets.contains(state.block)) {
                                    found.add(pos)
                                }
                            }
                        }
                    }
                    if (found.isNotEmpty()) {
                        cachedBlocks[chunkPos] = found
                    }
                }
            } finally {
                isScanning = false
            }
        }

    }
}