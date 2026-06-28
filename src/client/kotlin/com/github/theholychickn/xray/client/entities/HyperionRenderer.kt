package com.github.theholychickn.xray.client.entities

import com.github.theholychickn.xray.HyperionMod
import com.github.theholychickn.xray.entities.HyperionEntity
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.geom.ModelLayers
import net.minecraft.client.renderer.entity.ArmorModelSet
import net.minecraft.client.renderer.entity.EntityRendererProvider
import net.minecraft.client.renderer.entity.MobRenderer
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer
import net.minecraft.client.renderer.entity.state.ArmedEntityRenderState
import net.minecraft.client.renderer.entity.state.HumanoidRenderState
import net.minecraft.resources.Identifier
import net.minecraft.world.entity.EquipmentSlot

/**
 * Renders [com.github.theholychickn.xray.entities.HyperionEntity] as a white humanoid figure with armor and held items visible.
 *
 * Uses [net.minecraft.client.model.HumanoidModel] (the superclass of PlayerModel) because PlayerModel<T> is
 * constrained to `T : AbstractClientPlayer`, which HyperionEntity does not extend.
 * HumanoidModel gives the same head/body/arm/leg rig and UV layout — the all-white
 * skin texture makes every part appear white.
 * Texture: `assets/not-enough-racism/textures/entity/hyperion.png` (64×64 all-white PNG).
 *
 * ## armor rendering
 *
 *
 * ## held item rendering
 *
 */
class HyperionRenderer(context: EntityRendererProvider.Context) :
    MobRenderer<HyperionEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>>(
        context,
        HumanoidModel<HumanoidRenderState>(context.bakeLayer(ModelLayers.PLAYER)),
        0.5f // shadow radius, same as player
    ) {

    init {
        // armor layer
        addLayer(
            HumanoidArmorLayer(
                this,
                ModelLayers.PLAYER_ARMOR.map { layerLocation ->
                    HumanoidModel<HumanoidRenderState>(context.bakeLayer(layerLocation))
                },
                context.equipmentRenderer
            )
        )
        // held item layer
        addLayer(ItemInHandLayer(this))
    }

    override fun createRenderState(): HumanoidRenderState = HumanoidRenderState()

    /**
     * Populates render state from the entity before the frame is drawn.
     *
     * The super call handles position, rotation, and basic living entity state.
     * Equipment items need to be explicitly copied to the render state so that
     * [HumanoidArmorLayer] and [ItemInHandLayer] can read them.ch Mob already implements. If armor doesn't render,
     *         // you may need to set additional fields her
     *
     * The exact field names on [HumanoidRenderState] depend on your MC 26.x build.
     * Common patterns to try if the below doesn't compile:
     *   state.mainHandItem = entity.getMainHandItem()
     *   state.offHandItem  = entity.getOffhandItem()
     * or the layer may read from the entity directly in some versions.
     */

    private val itemModelResolver = context.itemModelResolver
    override fun extractRenderState(
        entity: HyperionEntity,
        state: HumanoidRenderState,
        partialTick: Float
    ) {
        super.extractRenderState(entity, state, partialTick)

        ArmedEntityRenderState.extractArmedEntityRenderState(entity, state, itemModelResolver, partialTick)

        // FIX: populate armor equipment fields so HumanoidArmorLayer renders gear correctly.
        state.headEquipment  = entity.getItemBySlot(EquipmentSlot.HEAD)
        state.chestEquipment = entity.getItemBySlot(EquipmentSlot.CHEST)
        state.legsEquipment  = entity.getItemBySlot(EquipmentSlot.LEGS)
        state.feetEquipment  = entity.getItemBySlot(EquipmentSlot.FEET)

    }

    override fun getTextureLocation(state: HumanoidRenderState): Identifier =
        HyperionMod.id("textures/entities/hyperion.png")
}