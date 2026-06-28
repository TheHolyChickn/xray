package com.github.theholychickn.xray.entities

import net.minecraft.ChatFormatting
import net.minecraft.core.BlockPos
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.sounds.SoundSource
import net.minecraft.world.InteractionHand
import net.minecraft.world.SimpleContainer
import net.minecraft.world.damagesource.DamageSource
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.PathfinderMob
import net.minecraft.world.entity.ai.attributes.AttributeSupplier
import net.minecraft.world.entity.item.ItemEntity
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.equipment.ArmorType
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.entity.ChestBlockEntity
import net.minecraft.world.level.block.entity.LecternBlockEntity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3
import kotlin.math.atan2
import kotlin.math.sqrt


/**
 * Hyperion — a humanoid entity controlled entirely by code.
 *
 * Registered as `not-enough-racism:hyperion`. Spawn with:
 * `/summon not-enough-racism:hyperion <x> <y> <z>`
 *
 * All `cmd*` methods are called from [HyperionCommands] on the server thread.
 * They are designed to be composable and will eventually be chained into action
 * sequences bound to episode keybinds.
 */
class HyperionEntity(type: EntityType<HyperionEntity>, level: Level) : PathfinderMob(type, level) {

    /**
     * 27 slot inventory, like that of a player without the hotbar.
     *
     * Minecraft already handles armor and equipped items, so this is for anything else Hyperion is carrying.
     */
    val inventory: SimpleContainer = SimpleContainer(27)

    init {
        // guraentee all items drop on death
        EquipmentSlot.entries.forEach { setDropChance(it, 2.0f) }
    }

    override fun dropCustomDeathLoot(level: ServerLevel, source: DamageSource, killedByPlayer: Boolean) {
        super.dropCustomDeathLoot(level, source, killedByPlayer)
        for (i in 0 until inventory.containerSize) {
            val stack = inventory.getItem(i)
            if (!stack.isEmpty) spawnAtLocation(level, stack)
        }
    }

    /** Hyperion has no autonomous goals and is controlled fully by commands. */
    override fun registerGoals() {}
    override fun canBeLeashed() = false
    override fun isPushable() = false
    override fun canBreatheUnderwater() = false

    // ── inventory ─────────────────────────────────────────────────────────────────────

    /**
     * Puts [stack] into the first empty slot of [inventory].
     * Returns false and does nothing if inventory full.
     */
    fun addToInventory(stack: ItemStack): Boolean {
        for (i in 0 until inventory.containerSize) {
            if (inventory.getItem(i).isEmpty) {
                inventory.setItem(i, stack)
                return true
            }
        }
        return false
    }

    /**
     * Puts [stack] in inventory slot [index] (0-26).
     */
    fun setInventorySlot(index: Int, stack: ItemStack) =
        inventory.setItem(index.coerceIn(0, inventory.containerSize - 1), stack)

    /** Returns the stack in inventory slot [index] (0-26) */
    fun getInventorySlot(index: Int) = inventory.getItem(index.coerceIn(0, inventory.containerSize - 1))

    /**
     * Equips [stack] into the slot that matches its type:
     * armor -> armor slot
     * Totems -> offhand
     * everything else -> MAINHAND
     */
    fun autoEquip(stack: ItemStack) {
        val slot = when (stack.item) {
            Items.TOTEM_OF_UNDYING -> EquipmentSlot.OFFHAND
            else -> stack.get(DataComponents.EQUIPPABLE)?.slot ?: EquipmentSlot.MAINHAND
        }
        setItemSlot(slot, stack)
    }

    /** Equips [stack] in [slot]. Thin wrapper around [setItemSlot] for call-site clarity. */
    fun equip(slot: EquipmentSlot, stack: ItemStack) = setItemSlot(slot, stack)

    /** Puts [stack] in Hyperion's main hand */
    fun holdMainHand(stack: ItemStack) = setItemSlot(EquipmentSlot.MAINHAND, stack)

    /** puts [stack] in Hyperion's off hand */
    fun holdOffHand(stack: ItemStack) = setItemSlot(EquipmentSlot.OFFHAND, stack)

    /** Clears every equipment and inventory slot */
    fun clearInventory() {
        EquipmentSlot.entries.forEach { setItemSlot(it, ItemStack.EMPTY) }
        for (i in 0 until inventory.containerSize) inventory.setItem(i, ItemStack.EMPTY)
    }

    /** Applies [loadout] to this entity - delegates to [HyperionLoadout.applyTo] */
    fun applyLoadout(loadout: HyperionLoadout) = loadout.applyTo(this)

    // ── movement ──────────────────────────────────────────────────────────────────────

    /** Disables gravity and stops any in-progress navigation */
    fun fly() {
        isNoGravity = true
        navigation.stop()
    }

    /** Re-enables gravity. Hyperion will fall to the ground. */
    fun land() {
        isNoGravity = false
    }

    /**
     * Teleports Hyperion to ([x],[y],[z]).
     * Preserves the flying state.
     */
    fun teleport(x: Double, y: Double, z: Double) {
        val wasFlying = isNoGravity
        moveOrInterpolateTo(Vec3(x, y, z), yRot, xRot)
        isNoGravity = wasFlying
        navigation.stop()
    }

    /**
     * Walks Hyperion to ([x], [y], [z]) using MC's built-in A* pathfinding.
     * Automatically handles jumping over obstacles.
     * [speed] is a multiplier on base movement speed (1.0 = normal).
     *
     * Note: this will disengage flying (sets gravity on) so Hyperion
     * stays on the ground. Call [cmdFly] to re-enable flight after walking.
     */
    fun walkTo(x: Double, y: Double, z: Double, speed: Double = 1.0) {
        isNoGravity = false
        navigation.moveTo(x, y, z, speed)
    }

    // ── look direction ────────────────────────────────────────────────────────────────

    /**
     * Points Hyperion's head in direction ([yaw], [pitch]).
     *
     * Yaw degrees:  0 = south, 90 = west, 180 = north, -90 = east.
     * Pitch degrees: 0 = horizontal, -90 = straight up, 90 = straight down.
     */
    fun look(yaw: Float, pitch: Float) {
        yRot = yaw
        xRot = pitch
        yHeadRot = yaw
        yBodyRot = yaw
    }

    /**
     * Rotates Hyperion's head to face world position ([x], [y], [z]).
     * [y] is compared against the eye height, not the foot position.
     */
    fun lookAt(x: Double, y: Double, z: Double) {
        val dx = x - this.x
        val dy = y - (this.y + eyeHeight)
        val dz = z - this.z
        val horizDist = sqrt(dx * dx + dz * dz)
        val pitch = (-Math.toDegrees(atan2(dy, horizDist))).toFloat()
        val yaw = Math.toDegrees(atan2(-dx, dz)).toFloat()
        look(yaw, pitch)
    }

    // ── chest interaction ─────────────────────────────────────────────────────────────

    /**
     * Plays the chest-open animation and sound at [pos].
     * Does nothing if there is no chest block entity at that position.
     */
    fun openChest(pos: BlockPos) {
        val sl = level() as? ServerLevel ?: return
        if (sl.getBlockEntity(pos) !is ChestBlockEntity) return
        sl.blockEvent(pos, sl.getBlockState(pos).block, 1, 1)
        sl.playSound(
            null, pos, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS,
            0.5f, sl.random.nextFloat() * 0.1f + 0.9f,
        )
    }

    /**
     * Plays the chest-close animation and sound at [pos].
     */
    fun closeChest(pos: BlockPos) {
        val sl = level() as? ServerLevel ?: return
        if (sl.getBlockEntity(pos) !is ChestBlockEntity) return
        sl.blockEvent(pos, sl.getBlockState(pos).block, 1, 0)
        sl.playSound(
            null, pos, SoundEvents.CHEST_CLOSE, SoundSource.BLOCKS,
            0.5f, sl.random.nextFloat() * 0.1f + 0.9f,
        )
    }

    // ── books ─────────────────────────────────────────────────────────────────────────

    /**
     * Places [book] on the lectern at [lecternPos].
     * [book] should be a `WrittenBook` item stack — see [HyperionCommands.createWrittenBook].
     */
    fun placeBook(lecternPos: BlockPos, book: ItemStack) {
        val sl = level() as? ServerLevel ?: return
        val blockEntity = sl.getBlockEntity(lecternPos) as? LecternBlockEntity ?: return
        blockEntity.book = book
        blockEntity.setChanged()
        val state = sl.getBlockState(lecternPos)
        sl.sendBlockUpdated(lecternPos, state, state, 3)
    }

    // ── blocks ────────────────────────────────────────────────────────────────────────

    /**
     * Places [state] at [pos] with the block-placement sound and arm swing.
     */
    fun placeBlock(pos: BlockPos, state: BlockState) {
        val sl = level() as? ServerLevel ?: return
        swing(InteractionHand.MAIN_HAND)
        sl.setBlockAndUpdate(pos, state)
        val sound = state.soundType
        sl.playSound(null, pos, sound.placeSound, SoundSource.BLOCKS,
            (sound.volume + 1f) / 2f, sound.pitch * 0.8f)
    }

    /**
     * Breaks the block at [pos] with the block-break sound, particles, and arm swing.
     * Set [drops] to true to spawn item drops.
     */
    fun breakBlock(pos: BlockPos, drops: Boolean = false) {
        swing(InteractionHand.MAIN_HAND)
        level().destroyBlock(pos, drops, this)
    }

    // ── chat ──────────────────────────────────────────────────────────────────────────

    /**
     * Broadcasts a system message in the format `Hyperion: <message>`.
     * Visible to all players in the world (in singleplayer, just the one player).
     */
    fun say(message: String) {
        level().server?.playerList?.broadcastSystemMessage(
            Component.empty()
                .append(Component.literal("Hyperion").withStyle(ChatFormatting.WHITE))
                .append(Component.literal(": $message")),
            false
        )
    }

    // ── items ─────────────────────────────────────────────────────────────────────────

    /**
     * Throws [stack] from Hyperion's eye position.
     * If [direction] is null, uses Hyperion's current look vector.
     * The item cannot be picked up for 2 seconds.
     */
    fun throwItem(stack: ItemStack, direction: Vec3? = null) {
        val dir  = direction ?: lookAngle
        val item = ItemEntity(level(), x, eyeY - 0.3, z, stack.copy())
        item.setDeltaMovement(dir.x * 0.4, dir.y * 0.4 + 0.1, dir.z * 0.4)
        item.setPickUpDelay(40)
        level().addFreshEntity(item)
        swing(InteractionHand.MAIN_HAND)
    }

    // ── misc ──────────────────────────────────────────────────────────────────────────

    /** Plays Hyperion's arm-swing animation visible to nearby clients. */
    fun swing() = swing(InteractionHand.MAIN_HAND)

    // ── persistence ───────────────────────────────────────────────────────────────────

    override fun addAdditionalSaveData(tag: ValueOutput) {
        super.addAdditionalSaveData(tag)
        tag.putBoolean("WasFlying", isNoGravity)
    }

    override fun readAdditionalSaveData(tag: ValueInput) {
        super.readAdditionalSaveData(tag)
        isNoGravity = tag.getBooleanOr("WasFlying", true)
    }

    companion object {
        fun createAttributes(): AttributeSupplier.Builder = createMobAttributes()
    }

}