package com.github.theholychickn.xray.client.entities

import com.github.theholychickn.xray.HyperionMod
import com.github.theholychickn.xray.entities.HyperionEntity
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.resources.Identifier

/**
 * Renders [com.github.theholychickn.xray.entities.HyperionEntity] as a white humanoid figure using the standard player skeleton.
 *
 * Uses [net.minecraft.client.model.HumanoidModel] (the superclass of PlayerModel) because PlayerModel<T> is
 * constrained to `T : AbstractClientPlayer`, which HyperionEntity does not extend.
 * HumanoidModel gives the same head/body/arm/leg rig and UV layout — the all-white
 * skin texture makes every part appear white.
 *
 * Texture: `assets/not-enough-racism/textures/entity/hyperion.png` (64×64 all-white PNG).
 */
class HyperionRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<HyperionEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>>(
        context,
        HumanoidModel<HumanoidRenderState>(context.bakeLayer(ModelLayers.PLAYER)),
        0.5f // shadow radius, same as player
    ) {

    override fun createRenderState(): HumanoidRenderState = HumanoidRenderState()

    override fun getTextureLocation(state: HumanoidRenderState): Identifier =
        HyperionMod.id("textures/entities/hyperion.png")
}