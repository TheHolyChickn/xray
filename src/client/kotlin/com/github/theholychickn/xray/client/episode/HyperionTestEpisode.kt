package com.github.theholychickn.xray.client.episode

import com.github.theholychickn.xray.HyperionMod
import com.github.theholychickn.xray.client.HyperionModClient
import com.github.theholychickn.xray.entities.HyperionAction
import com.github.theholychickn.xray.entities.HyperionEntity
import com.github.theholychickn.xray.entities.HyperionLoadout
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.phys.AABB
import org.lwjgl.glfw.GLFW

/**
 * Test episode for verifying Hyperion's inventory, armor, and held-item rendering.
 *
 * ## Keybinds (Numpad)
 * | Key     | Action |
 * |---------|--------|
 * | KP_1    | Spawn Hyperion at (0, 150, 0) with full default loadout |
 * | KP_2    | Armor test — strip then re-equip each slot one-by-one (1 s gap each) |
 * | KP_3    | Hand-item test — cycle main hand and off hand through several items |
 * | KP_4    | Inventory test — clear, add a stack of each tool, then say slot count |
 * | KP_0    | Kill all Hyperion entities in the world |
 *
 * ## Setup
 * 1. Set `activeEpisode` to `"hyperion_test"` in `config/not-enough-racism.json`.
 * 2. Create or use a **superflat** world with **cheats enabled**.
 * 3. Stand near (0, 0, 0) — Hyperion spawns at (0, 150, 0) directly above the origin.
 *    You need to be close enough that the chunk is loaded (within your render distance).
 *    The easiest way: `/tp 0 80 0` before pressing KP_1.
 * 4. Enable flying (`/gamemode creative` or `/gamemode spectator`) so you can observe
 *    Hyperion at y=150 without falling.
 */
class HyperionTestEpisode : Episode(
    id = "hyperion_test",
    shouldRenderThoughts = false,
) {

    companion object {
        private const val SPAWN_X = 0.0
        private const val SPAWN_Y = -58.0
        private const val SPAWN_Z = 0.0
    }

    // ── helpers ───────────────────────────────────────────────────────────────────────

    /** Returns all Hyperion entities in the overworld. */
    private fun findAll(server: MinecraftServer): List<HyperionEntity> =
        server.overworld().getEntitiesOfClass(HyperionEntity::class.java, WORLD_BOX)

    /** Returns the first Hyperion entity found, or null. Prints a hint if none. */
    private fun findOrWarn(server: MinecraftServer, client: Minecraft): HyperionEntity? {
        val entity = findAll(server).firstOrNull()
        if (entity == null) {
            client.player?.sendSystemMessage(
                Component.literal("[HyperionTest] No Hyperion entity found. Press KP_1 to spawn.")
            )
        }
        return entity
    }

    // ── keybinds ──────────────────────────────────────────────────────────────────────

    override val keybinds: Map<KeyMapping, (Minecraft) -> Unit> = mapOf(

        // ── KP_1: Spawn ───────────────────────────────────────────────────────────────

        KeyMapping(
            "key.hyperion_test.spawn",
            GLFW.GLFW_KEY_1,
            HyperionModClient.category
        ) to { client ->
            client.onServer {
                overworld().getEntitiesOfClass(HyperionEntity::class.java, WORLD_BOX)
                    .forEach { it.remove(Entity.RemovalReason.DISCARDED) }
                val level = overworld()
                val entity = HyperionEntity(HyperionMod.HYPERION_ENTITY, level).also { e ->
                    e.setPos(SPAWN_X, SPAWN_Y, SPAWN_Z)
                    HyperionLoadout.Builder(level.registryAccess()).defaultLoadout.applyTo(e)
                }
                level.addFreshEntity(entity)
            }
            client.player?.sendSystemMessage(
                Component.literal("[HyperionTest] Spawned at (${SPAWN_X.toInt()}, ${SPAWN_Y.toInt()}, ${SPAWN_Z.toInt()}) with default loadout.")
            )
        },

        // ── KP_2: Armor test ──────────────────────────────────────────────────────────
        // Clears all armor, then re-equips each slot one at a time with a 1-second
        // (20-tick) gap so you can watch the model update slot by slot.

        KeyMapping(
            "key.hyperion_test.armor",
            GLFW.GLFW_KEY_2,
            HyperionModClient.category
        ) to { client ->
            client.withHyperion { hyperion ->
                val builder = HyperionLoadout.Builder(hyperion.registryAccess())
                HyperionAction.Builder(hyperion).send {
                    say("Armor test starting — stripping all slots.")
                    clearInventory()
                    wait(20)            // 1 s — observe naked model

                    say("Equipping helmet.")
                    equip(EquipmentSlot.HEAD, builder.maxHelmet)
                    wait(20)

                    say("Equipping chestplate.")
                    equip(EquipmentSlot.CHEST, builder.maxChestplate)
                    wait(20)

                    say("Equipping leggings.")
                    equip(EquipmentSlot.LEGS, builder.maxLegs)
                    wait(20)

                    say("Equipping boots.")
                    equip(EquipmentSlot.FEET, builder.maxBoots)
                    wait(20)

                    say("Armor test complete. All four slots occupied.")
                }.build().invoke()
            }
        },

        // ── KP_3: Hand-item test ──────────────────────────────────────────────────────
        // Cycles through several main-hand items and two off-hand items so you can
        // confirm ItemInHandLayer is rendering correctly for different item types.

        KeyMapping(
            "key.hyperion_test.hands",
            GLFW.GLFW_KEY_3,
            HyperionModClient.category
        ) to { client ->
            client.withHyperion { hyperion ->
                val builder  = HyperionLoadout.Builder(hyperion.registryAccess())

                HyperionAction.Builder(hyperion).send {
                    say("Hand-item test starting.")

                    // ── main hand cycling ────────────────────────────────────────────
                    say("Main hand: sword.")
                    holdMainHand(builder.maxSword)
                    wait(20)

                    say("Main hand: fortune pick.")
                    holdMainHand(builder.fortunePick)
                    wait(20)

                    say("Main hand: silk pick.")
                    holdMainHand(builder.silkPick)
                    wait(20)

                    say("Main hand: bow.")
                    holdMainHand(ItemStack(Items.BOW))
                    wait(20)

                    say("Main hand: written book (flat item).")
                    holdMainHand(ItemStack(Items.WRITTEN_BOOK))
                    wait(20)

                    say("Main hand: empty.")
                    holdMainHand(ItemStack.EMPTY)
                    wait(20)

                    // ── off-hand cycling ─────────────────────────────────────────────
                    say("Off hand: totem of undying.")
                    holdOffHand(ItemStack(Items.TOTEM_OF_UNDYING))
                    wait(20)

                    say("Off hand: shield.")
                    holdOffHand(ItemStack(Items.SHIELD))
                    wait(20)

                    say("Off hand: empty.")
                    holdOffHand(ItemStack.EMPTY)
                    wait(20)

                    say("Hand-item test complete.")
                }.build().invoke()
            }
        },

        // ── KP_4: Inventory test ──────────────────────────────────────────────────────
        // Clears the 27-slot inventory then fills it with distinct tool stacks so you
        // can verify addToInventory() packs correctly and the slot count is right.

        KeyMapping(
            "key.hyperion_test.inventory",
            GLFW.GLFW_KEY_4,
            HyperionModClient.category
        ) to { client ->
            client.withHyperion { hyperion ->
                val builder  = HyperionLoadout.Builder(hyperion.registryAccess())

                HyperionAction.Builder(hyperion).send {
                    say("Inventory test: clearing.")
                    clearInventory()
                    wait(10)

                    // Fill with a representative spread of item types.
                    say("Adding tools and consumables to inventory.")
                    giveItem(builder.fortunePick)
                    giveItem(builder.silkPick)
                    giveItem(builder.fortuneShovel)
                    giveItem(builder.silkShovel)
                    giveItem(builder.maxSword)
                    giveItem(ItemStack(Items.ENDER_CHEST, 64))
                    giveItem(ItemStack(Items.SHULKER_BOX))
                    giveItem(ItemStack(Items.TOTEM_OF_UNDYING, 3))
                    giveItem(ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 4))
                    giveItem(ItemStack(Items.ENDER_PEARL, 16))
                    giveItem(ItemStack(Items.FIREWORK_ROCKET, 64))
                    giveItem(ItemStack(Items.WATER_BUCKET))
                    giveItem(ItemStack(Items.WRITABLE_BOOK, 4))
                    wait(10)

                    // Count filled slots and report.
                    // This runs inside the scheduler so the entity state is final by now.
                }.build().also { seq ->
                    // Run the sequence, then schedule a slot-count report one tick later
                    // so it fires after all the giveItem steps have completed.
                    seq.invoke()
                    // 20 ticks: enough for clearInventory (t=0) + giveItem chain (t=10) + margin
                    com.github.theholychickn.xray.TickScheduler.schedule(30) {
                        val filledSlots = (0 until hyperion.inventory.containerSize)
                            .count { !hyperion.inventory.getItem(it).isEmpty }
                        hyperion.say("Inventory filled: $filledSlots / ${hyperion.inventory.containerSize} slots occupied.")
                    }
                }
            }
        },

        // ── KP_0: Kill all Hyperion entities ─────────────────────────────────────────

        KeyMapping(
            "key.hyperion_test.kill",
            GLFW.GLFW_KEY_0,
            HyperionModClient.category
        ) to { client ->
            client.onServer {
                val all = overworld().getEntitiesOfClass(HyperionEntity::class.java, WORLD_BOX)
                all.forEach { it.remove(Entity.RemovalReason.DISCARDED) }
                client.player?.sendSystemMessage(
                    Component.literal("[HyperionTest] Removed ${all.size} Hyperion instance(s).")
                )
            }
        },
    )

    override fun reset() {
        // Nothing to restore — this episode has no block changes.
    }
}