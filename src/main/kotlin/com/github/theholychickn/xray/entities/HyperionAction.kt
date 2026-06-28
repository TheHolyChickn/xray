package com.github.theholychickn.xray.entities

import com.github.theholychickn.xray.TickScheduler
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.state.BlockState

/**
 * Fluent builder for scripted Hyperion action sequences.
 *
 * Actions are collected in order. [wait] inserts a tick-level delay between the
 * preceding and following actions. Calling [build] compiles the sequence into a
 * single `() -> Unit` that, when invoked, submits all actions to [TickScheduler]
 * with their accumulated delays.
 *
 * ## Example
 * ```kotlin
 * val sequence = HyperionAction.Builder(entity).send {
 *     teleport(10, 64, -5)
 *     wait(10)                       // 0.5 s at 20 tps
 *     openChest(BlockPos(10, 64, -4))
 *     wait(40)                       // 2 s
 *     closeChest(BlockPos(10, 64, -4))
 *     wait(5)
 *     fly()
 * }.build()
 *
 * // In a keybind lambda — dispatch to server thread first:
 * KeyMapping("...") to { client ->
 *     client.singleplayerServer?.execute { sequence.invoke() }
 * }
 * ```
 *
 * ## Thread safety
 * The compiled lambda must be invoked on the server thread so that [TickScheduler]
 * and the entity methods run in the same thread context.
 * See [TickScheduler] for details.
 *
 * @param entity The [HyperionEntity] to register the action chian for.
 */
class HyperionAction(private val entity: HyperionEntity) {

    // ── internal action representation ────────────────────────────────────────────────

    private sealed class Action {
        /** An action to execute at the accumulated tick offset */
        data class Do(val action: () -> Unit): Action()
        /** A delay to add to the tick offset before subsequent actions */
        data class Wait(val ticks: Int): Action()
    }

    private val actions = mutableListOf<Action>()

    // ── action DSL ────────────────────────────────────────────────────────────────────

    fun teleport(x: Double, y: Double, z: Double) { actions += Action.Do { entity.teleport(x, y, z) } }
    fun teleport(x: Int, y: Int, z: Int) = teleport(x.toDouble(), y.toDouble(), z.toDouble())
    fun look(yaw: Float, pitch: Float) { actions += Action.Do { entity.look(yaw, pitch) } }
    fun lookAt(x: Double, y: Double, z: Double) { actions += Action.Do { entity.lookAt(x, y, z) } }
    fun walk(x: Double, y: Double, z: Double, speed: Double = 1.0) { actions += Action.Do { entity.walkTo(x, y, z, speed) } }
    fun fly(enable: Boolean = true) {
        actions += Action.Do { if (enable) entity.fly() else entity.land() }
    }
    fun say(message: String) { actions += Action.Do { entity.say(message) } }
    fun openChest(pos: BlockPos) { actions += Action.Do { entity.openChest(pos) } }
    fun closeChest(pos: BlockPos) { actions += Action.Do { entity.closeChest(pos) } }
    fun placeBlock(pos: BlockPos, state: BlockState) { actions += Action.Do { entity.placeBlock(pos, state) } }
    fun breakBlock(pos: BlockPos, drops: Boolean = false) { actions += Action.Do { entity.breakBlock(pos, drops) } }
    fun throwItem(stack: ItemStack) { actions += Action.Do { entity.throwItem(stack) } }
    fun placeBook(pos: BlockPos, book: ItemStack) { actions += Action.Do { entity.placeBook(pos, book) } }
    fun swing() { actions += Action.Do { entity.swing() } }
    fun wait(ticks: Int = 1) { actions += Action.Wait(ticks) }
    fun equip(slot: EquipmentSlot, stack: ItemStack) { actions += Action.Do { entity.equip(slot, stack) } }
    fun holdMainHand(stack: ItemStack) { actions += Action.Do { entity.holdMainHand(stack) } }
    fun holdOffHand(stack: ItemStack) { actions += Action.Do { entity.holdOffHand(stack) } }
    fun autoEquip(stack: ItemStack) { actions += Action.Do { entity.autoEquip(stack) } }
    fun clearInventory() { actions += Action.Do { entity.clearInventory() } }
    fun giveItem(stack: ItemStack) { actions += Action.Do { entity.addToInventory(stack) } }

    // ── compilation ───────────────────────────────────────────────────────────────────

    /**
     * Compiles this sequence into a single `() -> Unit`.
     *
     * When invoked, each action is submitted to [TickScheduler] at the correct
     * tick offset. The returned lambda is safe to store and invoke multiple times
     * (each invocation starts a fresh scheduling run from the current tick).
     */
    fun build(): () -> Unit {
        val capturedActions = actions.toList()
        return {
            var offset = 0
            capturedActions.forEach { task ->
                when (task) {
                    is Action.Wait -> offset += task.ticks
                    is Action.Do -> {
                        // capture the offset by value; otherwise the lambda would close over
                        // the mutable variable and all actions would see the final value
                        val tickOffset = offset
                        val taskAction = task.action
                        TickScheduler.schedule(tickOffset) { taskAction() }
                    }
                }
            }
        }
    }

    // ── builder ───────────────────────────────────────────────────────────────────────

    /**
     * Fluent entry point for constructing action chains.
     *
     * ```kotlin
     * val cmd = HyperionAction.Builder(entity).send {
     *     teleport(10, 3, 11)
     *     wait(20)
     *     say("Done.")
     * }.build()
     * ```
     *
     * @param entity the [HyperionEntity] to register the action chain for.
     */
    class Builder(private val entity: HyperionEntity) {
        /**
         * Command block Hyperion should execute.
         *
         * @param block The actions Hyperion should perform, in order.
         * @return A [HyperionAction] with an updated [actions] list. Call [build] on this object immediately.
         */
        fun send(block: HyperionAction.() -> Unit): HyperionAction =
            HyperionAction(this.entity).apply(block)
    }
}