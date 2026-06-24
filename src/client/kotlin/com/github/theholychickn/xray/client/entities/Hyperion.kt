package com.github.theholychickn.xray.client.entities

import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.geom.ModelPart
import net.minecraft.client.model.geom.PartNames
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
import net.minecraft.world.entity.ai.goal.RandomStrollGoal
import net.minecraft.world.entity.ai.goal.TemptGoal
import net.minecraft.world.entity.ai.memory.WalkTarget
import net.minecraft.world.entity.animal.cow.Cow
import net.minecraft.world.item.Items
import net.minecraft.world.item.crafting.Ingredient
import net.minecraft.world.level.Level

open class Hyperion(
    entityType: EntityType<out PathfinderMob>,
    world: Level
) : PathfinderMob(entityType, world) {

    // constructor(world: Level) : this(entityType = ModEntityTypes.HYPERION, world)

    companion object {
        @JvmStatic
        fun createCubeAttributes(): AttributeSupplier.Builder {
            return PathfinderMob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 5.0)
                .add(Attributes.TEMPT_RANGE, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.3)
        }
    }

    override fun registerGoals() {
        this.goalSelector.addGoal(0, TemptGoal(this, 1.0, Ingredient.of(Items.WHEAT), false))
        this.goalSelector.addGoal(1, RandomStrollGoal(this, 1.0))
        this.goalSelector.addGoal(2, LookAtPlayerGoal(this, Cow::class.java, 4F))
        this.goalSelector.addGoal(3, RandomLookAroundGoal(this))
    }
}

open class HyperionEntityRenderState : LivingEntityRenderState() {
}

open class HyperionEntityModel(root: ModelPart) : EntityModel<HyperionEntityRenderState>(root) {
    private val head: ModelPart = root.getChild(PartNames.HEAD)
    private val leftLeg: ModelPart = root.getChild(PartNames.LEFT_LEG)
    private val rightLeg: ModelPart = root.getChild(PartNames.RIGHT_LEG)
}