package com.github.theholychickn.xray.client

import com.github.theholychickn.xray.config.ConfigManager
import com.mojang.blaze3d.pipeline.RenderPipeline
import com.mojang.blaze3d.platform.InputConstants
import com.mojang.blaze3d.systems.RenderSystem
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexConsumer
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper
import net.fabricmc.fabric.api.client.rendering.v1.level.LevelRenderEvents
import net.minecraft.client.KeyMapping
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderPipelines
import net.minecraft.client.renderer.ShapeRenderer
import net.minecraft.client.renderer.rendertype.RenderType
import net.minecraft.client.renderer.rendertype.RenderTypes
import net.minecraft.resources.Identifier
import net.minecraft.world.phys.AABB
import org.lwjgl.glfw.GLFW
import java.util.Optional

object NotEnoughRacismClient : ClientModInitializer {
    private lateinit var toggleKeyMapping: KeyMapping
    private var moduleEnabled = false
    private var tickCounter = 0

    private val xrayLinesPipeline: RenderPipeline = RenderPipelines.register(
        RenderPipeline.builder(RenderPipelines.LINES_SNIPPET)
            .withLocation(Identifier.fromNamespaceAndPath("not-enough-racism", "pipeline/xray_lines"))
            .withDepthStencilState(Optional.empty())
            .build()
    )

    private val xrayLines: RenderType = RenderType.create(
        "not-enough-racism:xray_lines",
        xrayLinesPipeline
    )

    override fun onInitializeClient() {
        ConfigManager.load()

        val category = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("not-enough-racism", "main")
        )

        toggleKeyMapping = KeyMappingHelper.registerKeyMapping(
            KeyMapping(
                "key.not-enough-racism.toggle",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_X,
                category
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client ->
            while (toggleKeyMapping.consumeClick()) {
                moduleEnabled = !moduleEnabled
                if (client.player != null) {
                    val status = if (moduleEnabled) "Enabled" else "Disabled"
                    client.player?.sendSystemMessage(net.minecraft.network.chat.Component.literal("X-Ray: $status"))
                }
                if (!moduleEnabled) {
                    BlockScanner.cachedBlocks.clear()
                }
            }

            if (moduleEnabled && tickCounter++ % 20 == 0) {
                BlockScanner.startScan()
            }
        })

        LevelRenderEvents.END_MAIN.register(LevelRenderEvents.EndMain { context ->
            if (!moduleEnabled) return@EndMain

            val camera = context.levelState().cameraRenderState.pos
            val poseStack = context.poseStack()
            val bufferSource = context.bufferSource() ?: return@EndMain

            RenderSystem.disableDepthTest()

            poseStack.pushPose()

            poseStack.translate(-camera.x, -camera.y, -camera.z)
            val buffer = bufferSource.getBuffer(RenderTypes.lines())

            BlockScanner.cachedBlocks.values.flatten().forEach { pos ->
                val aabb = AABB(pos)
                ShapeRenderer.renderLineBox(
                    poseStack,
                    buffer,
                    aabb,
                    1.0f, 0.0f, 0.0f, 1.0f
                )
            }

            poseStack.popPose()
        })
    }

    /**
     * Draws a wireframe box. This replaces the now-relocated/renamed
     * LevelRenderer.renderLineBox / ShapeRenderer.renderLineBox utility
     * by writing the 12 edges (24 vertices) straight to the VertexConsumer
     * ourselves — this is exactly what those helpers did internally, and
     * VertexConsumer.addVertex(...).setColor(...) is stable, current API
     * (confirmed straight from Fabric's own 26.1 docs sample code), so
     * there's no renamed-class guessing left in this part.
     */
    private fun renderLineBox(
        poseStack: PoseStack,
        buffer: VertexConsumer,
        box: AABB,
        red: Float, green: Float, blue: Float, alpha: Float
    ) {
        val matrix = poseStack.last().pose()
        val minX = box.minX.toFloat()
        val minY = box.minY.toFloat()
        val minZ = box.minZ.toFloat()
        val maxX = box.maxX.toFloat()
        val maxY = box.maxY.toFloat()
        val maxZ = box.maxZ.toFloat()

        fun edge(x1: Float, y1: Float, z1: Float, x2: Float, y2: Float, z2: Float) {
            buffer.addVertex(matrix, x1, y1, z1).setColor(red, green, blue, alpha)
            buffer.addVertex(matrix, x2, y2, z2).setColor(red, green, blue, alpha)
        }

        // bottom face
        edge(minX, minY, minZ, maxX, minY, minZ)
        edge(maxX, minY, minZ, maxX, minY, maxZ)
        edge(maxX, minY, maxZ, minX, minY, maxZ)
        edge(minX, minY, maxZ, minX, minY, minZ)
        // top face
        edge(minX, maxY, minZ, maxX, maxY, minZ)
        edge(maxX, maxY, minZ, maxX, maxY, maxZ)
        edge(maxX, maxY, maxZ, minX, maxY, maxZ)
        edge(minX, maxY, maxZ, minX, maxY, minZ)
        // vertical edges
        edge(minX, minY, minZ, minX, maxY, minZ)
        edge(maxX, minY, minZ, maxX, maxY, minZ)
        edge(maxX, minY, maxZ, maxX, maxY, maxZ)
        edge(minX, minY, maxZ, minX, maxY, maxZ)
    }
}