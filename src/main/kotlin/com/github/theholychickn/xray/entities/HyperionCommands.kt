package com.github.theholychickn.xray.entities

import com.github.theholychickn.xray.HyperionMod
import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.arguments.DoubleArgumentType
import com.mojang.brigadier.arguments.FloatArgumentType
import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.Commands
import net.minecraft.commands.arguments.blocks.BlockStateArgument
import net.minecraft.commands.arguments.coordinates.BlockPosArgument
import net.minecraft.commands.arguments.item.ItemArgument
import net.minecraft.core.component.DataComponents
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.network.Filterable
import net.minecraft.server.permissions.Permissions
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.component.WrittenBookContent
import net.minecraft.world.phys.AABB

/**
 * All `/hyperion` sub-commands.
 *
 * Requires operator level 2. Full command list:
 * ```
 * /hyperion fly
 * /hyperion land
 * /hyperion tp <x> <y> <z>
 * /hyperion look <yaw> <pitch>
 * /hyperion lookat <x> <y> <z>
 * /hyperion walk <x> <y> <z> [speed]
 * /hyperion say <message>
 * /hyperion throw <item> [count]
 * /hyperion place <x> <y> <z> <block_state>
 * /hyperion break <x> <y> <z> [drops]
 * /hyperion chest open <x> <y> <z>
 * /hyperion chest close <x> <y> <z>
 * /hyperion book <x> <y> <z> <title> <content>
 * /hyperion swing
 * ```
 */
object HyperionCommands {

    fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildCtx: CommandBuildContext) {
        dispatcher.register(
            Commands.literal("hyperion")
                .requires { it.permissions().hasPermission(Permissions.COMMANDS_MODERATOR) }
                .then(fly())
                .then(land())
                .then(tp())
                .then(look())
                .then(lookAt())
                .then(walk())
                .then(say())
                .then(throwCmd(buildCtx))
                .then(place(buildCtx))
                .then(breakCmd())
                .then(chest())
                .then(book())
                .then(swing())
                .then(spawn())
                .then(inv(buildCtx))
        )
    }

    // ── helpers ───────────────────────────────────────────────────────────────────────

    /**
     * Finds the Hyperion entity in the command source's level.
     * Warns if multiple instances exist and returns the one nearest the command source.
     */
    private fun getHyperion(source: CommandSourceStack): HyperionEntity? {
        val worldBox = AABB(-3.0E7, -3.2E2, -3.0E7, 3.0E7, 3.2E2, 3.0E7)
        val entities = source.level.getEntitiesOfClass(HyperionEntity::class.java, worldBox)
        return when {
            entities.isEmpty() -> {
                source.sendFailure(
                    Component.literal("Hyperion is not in the world. " +
                            "Spawn him with /summon not-enough-racism:hyperion <x> <y> <z>")
                )
                null
            }
            entities.size > 1 -> {
                source.sendSystemMessage(
                    Component.literal("Warning: ${entities.size} Hyperion instances. Using nearest.")
                )
                val srcPos = source.position
                entities.minByOrNull { it.distanceToSqr(srcPos) }
            }
            else -> entities.first()
        }
    }

    private fun slotFromString(s: String): EquipmentSlot? = when (s.lowercase()) {
        "head", "helmet"      -> EquipmentSlot.HEAD
        "chest", "chestplate" -> EquipmentSlot.CHEST
        "legs", "leggings"    -> EquipmentSlot.LEGS
        "feet", "boots"       -> EquipmentSlot.FEET
        "mainhand", "hand"    -> EquipmentSlot.MAINHAND
        "offhand"             -> EquipmentSlot.OFFHAND
        else                  -> null
    }

    /**
     * Creates a written book [ItemStack] with [author], [title], and a single page of [content].
     *
     * NOTE: In MC 1.20.5+ books use DataComponents, not raw NBT.
     * If this does not compile, you need to adjust the DataComponent API for your MC version:
     *
     * ```kt
     * val stack = ItemStack(Items.WRITTEN_BOOK)
     * val pages = listOf(Filterable.passThrough(Component.literal(content)))
     * stack.set(DataComponents.WRITTEN_BOOK_CONTENT,
     *     WrittenBookContent(Filterable.passThrough(title), author, 0, pages, true))
     * return stack
     * ```
     */
    fun createWrittenBook(author: String, title: String, content: String): ItemStack {
        val stack = ItemStack(Items.WRITTEN_BOOK)
        // DataComponents API — adjust class paths for your MC version:
        val pages: List<Filterable<Component>> = listOf(
            Filterable.passThrough(Component.literal(content))
        )
        stack.set(
            DataComponents.WRITTEN_BOOK_CONTENT,
            WrittenBookContent(
                Filterable.passThrough(title),
                author,
                0,
                pages,
                true
            )
        )
        return stack
    }

    // ── commands ──────────────────────────────────────────────────────────────────────

    private fun fly() = Commands.literal("fly").executes { ctx ->
        getHyperion(ctx.source)?.fly()
        1
    }

    private fun land() = Commands.literal("land").executes { ctx ->
        getHyperion(ctx.source)?.land()
        1
    }

    private fun tp() = Commands.literal("tp")
        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
            .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                    .executes { ctx ->
                        val x = DoubleArgumentType.getDouble(ctx, "x")
                        val y = DoubleArgumentType.getDouble(ctx, "y")
                        val z = DoubleArgumentType.getDouble(ctx, "z")
                        getHyperion(ctx.source)?.teleport(x, y, z)
                        1
                    })))

    private fun look() = Commands.literal("look")
        .then(Commands.argument("yaw",   FloatArgumentType.floatArg(-180f, 180f))
            .then(Commands.argument("pitch", FloatArgumentType.floatArg(-90f, 90f))
                .executes { ctx ->
                    val yaw   = FloatArgumentType.getFloat(ctx, "yaw")
                    val pitch = FloatArgumentType.getFloat(ctx, "pitch")
                    getHyperion(ctx.source)?.look(yaw, pitch)
                    1
                }))

    private fun lookAt() = Commands.literal("lookat")
        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
            .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                    .executes { ctx ->
                        val x = DoubleArgumentType.getDouble(ctx, "x")
                        val y = DoubleArgumentType.getDouble(ctx, "y")
                        val z = DoubleArgumentType.getDouble(ctx, "z")
                        getHyperion(ctx.source)?.lookAt(x, y, z)
                        1
                    })))

    private fun walk() = Commands.literal("walk")
        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
            .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                    // optional speed argument
                    .then(Commands.argument("speed", DoubleArgumentType.doubleArg(0.1, 10.0))
                        .executes { ctx ->
                            val x = DoubleArgumentType.getDouble(ctx, "x")
                            val y = DoubleArgumentType.getDouble(ctx, "y")
                            val z = DoubleArgumentType.getDouble(ctx, "z")
                            val s = DoubleArgumentType.getDouble(ctx, "speed")
                            getHyperion(ctx.source)?.walkTo(x, y, z, s)
                            1
                        })
                    .executes { ctx ->
                        val x = DoubleArgumentType.getDouble(ctx, "x")
                        val y = DoubleArgumentType.getDouble(ctx, "y")
                        val z = DoubleArgumentType.getDouble(ctx, "z")
                        getHyperion(ctx.source)?.walkTo(x, y, z)
                        1
                    })))

    private fun say() = Commands.literal("say")
        .then(Commands.argument("message", StringArgumentType.greedyString())
            .executes { ctx ->
                val msg = StringArgumentType.getString(ctx, "message")
                getHyperion(ctx.source)?.say(msg)
                1
            })

    private fun throwCmd(buildCtx: CommandBuildContext) = Commands.literal("throw")
        .then(Commands.argument("item", ItemArgument.item(buildCtx))
            .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                .executes { ctx ->
                    val hyperion = getHyperion(ctx.source) ?: return@executes 0
                    val count = IntegerArgumentType.getInteger(ctx, "count")
                    hyperion.throwItem(ItemArgument.getItem(ctx, "item").createItemStack(count))
                    1
                })
            .executes { ctx ->
                val hyperion = getHyperion(ctx.source) ?: return@executes 0
                hyperion.throwItem(ItemArgument.getItem(ctx, "item").createItemStack(1))
                1
            })

    private fun place(buildCtx: CommandBuildContext) = Commands.literal("place")
        .then(Commands.argument("pos", BlockPosArgument.blockPos())
            .then(Commands.argument("block", BlockStateArgument.block(buildCtx))
                .executes { ctx ->
                    val hyperion = getHyperion(ctx.source) ?: return@executes 0
                    val pos   = BlockPosArgument.getBlockPos(ctx, "pos")
                    val state = BlockStateArgument.getBlock(ctx, "block").state
                    hyperion.placeBlock(pos, state)
                    1
                }))

    private fun breakCmd() = Commands.literal("break")
        .then(Commands.argument("pos", BlockPosArgument.blockPos())
            .then(Commands.literal("drops")
                .executes { ctx ->
                    val hyperion = getHyperion(ctx.source) ?: return@executes 0
                    hyperion.breakBlock(BlockPosArgument.getBlockPos(ctx, "pos"), drops = true)
                    1
                })
            .executes { ctx ->
                val hyperion = getHyperion(ctx.source) ?: return@executes 0
                hyperion.breakBlock(BlockPosArgument.getBlockPos(ctx, "pos"))
                1
            })

    private fun chest() = Commands.literal("chest")
        .then(Commands.literal("open")
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes { ctx ->
                    getHyperion(ctx.source)?.openChest(BlockPosArgument.getBlockPos(ctx, "pos"))
                    1
                }))
        .then(Commands.literal("close")
            .then(Commands.argument("pos", BlockPosArgument.blockPos())
                .executes { ctx ->
                    getHyperion(ctx.source)?.closeChest(BlockPosArgument.getBlockPos(ctx, "pos"))
                    1
                }))

    /**
     * Places a written book on a lectern.
     * Usage: `/hyperion book <x> <y> <z> <title> <content>`
     * Title is a single word; content is greedy (can contain spaces).
     *
     * Example:
     * `/hyperion book 10 64 -5 "Notes" The work begins. I must speak.`
     */
    private fun book() = Commands.literal("book")
        .then(Commands.argument("pos", BlockPosArgument.blockPos())
            .then(Commands.argument("title", StringArgumentType.word())
                .then(Commands.argument("content", StringArgumentType.greedyString())
                    .executes { ctx ->
                        val hyperion = getHyperion(ctx.source) ?: return@executes 0
                        val pos     = BlockPosArgument.getBlockPos(ctx, "pos")
                        val title   = StringArgumentType.getString(ctx, "title")
                        val content = StringArgumentType.getString(ctx, "content")
                        hyperion.placeBook(pos, createWrittenBook("Hyperion", title, content))
                        1
                    })))

    private fun swing() = Commands.literal("swing").executes { ctx ->
        getHyperion(ctx.source)?.swing()
        1
    }

    // ── spawn ─────────────────────────────────────────────────────────────────────────

    /**
     * `/hyperion spawn <x> <y> <z>` — spawns with no items.
     * `/hyperion spawn <x> <y> <z> default` — spawns with [HyperionLoadout.Builder.defaultLoadout].
     */
    private fun spawn() = Commands.literal("spawn")
        .then(Commands.argument("x", DoubleArgumentType.doubleArg())
            .then(Commands.argument("y", DoubleArgumentType.doubleArg())
                .then(Commands.argument("z", DoubleArgumentType.doubleArg())
                    .then(Commands.literal("default")
                        .executes { ctx ->
                            spawnHyperion(
                                ctx.source,
                                DoubleArgumentType.getDouble(ctx, "x"),
                                DoubleArgumentType.getDouble(ctx, "y"),
                                DoubleArgumentType.getDouble(ctx, "z"),
                                HyperionLoadout.Builder(ctx.source.registryAccess()).defaultLoadout
                            )
                        }
                    )
                    .executes { ctx ->
                        spawnHyperion(ctx.source,
                            DoubleArgumentType.getDouble(ctx, "x"),
                            DoubleArgumentType.getDouble(ctx, "y"),
                            DoubleArgumentType.getDouble(ctx, "z"),
                            null
                        )
                    }
                )
            )
        )

    private fun spawnHyperion(
        source: CommandSourceStack,
        x: Double, y: Double, z: Double,
        loadout: HyperionLoadout?
    ): Int {
        val level  = source.level as? ServerLevel ?: return 0
        val entity = HyperionEntity(HyperionMod.HYPERION_ENTITY, level)
        entity.setPos(x, y, z)
        loadout?.applyTo(entity)
        level.addFreshEntity(entity)
        source.sendSystemMessage(Component.literal(
            "Hyperion spawned at ${x.toInt()}, ${y.toInt()}, ${z.toInt()}" +
                    if (loadout != null) " with default loadout." else "."
        ))
        return 1
    }

    // ── inv ───────────────────────────────────────────────────────────────────────────

    /**
     * `/hyperion inv give <item> [count]`   — add item to Hyperion's general inventory.
     * `/hyperion inv hold <item> [count]`   — put item in main hand.
     * `/hyperion inv offhand <item>`         — put item in off hand.
     * `/hyperion inv wear <item>`            — auto-equip armor to correct slot.
     * `/hyperion inv equip <slot> <item>`    — equip item to named slot.
     *                                          Slots: head/chest/legs/feet/mainhand/offhand
     * `/hyperion inv clear`                  — clear all slots and inventory.
     * `/hyperion inv default`                — apply [HyperionDefaultLoadout].
     */
    private fun inv(buildCtx: CommandBuildContext) = Commands.literal("inv")

        .then(Commands.literal("give")
            .then(Commands.argument("item", ItemArgument.item(buildCtx))
                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                    .executes { ctx ->
                        val hyperion = getHyperion(ctx.source) ?: return@executes 0
                        val stack = ItemArgument.getItem(ctx, "item")
                            .createItemStack(IntegerArgumentType.getInteger(ctx, "count"))
                        if (!hyperion.addToInventory(stack)) {
                            ctx.source.sendFailure(Component.literal("Hyperion's inventory is full."))
                        }
                        1
                    })
                .executes { ctx ->
                    val hyperion = getHyperion(ctx.source) ?: return@executes 0
                    hyperion.addToInventory(ItemArgument.getItem(ctx, "item").createItemStack(1))
                    1
                }))

        .then(Commands.literal("hold")
            .then(Commands.argument("item", ItemArgument.item(buildCtx))
                .then(Commands.argument("count", IntegerArgumentType.integer(1, 64))
                    .executes { ctx ->
                        val hyperion = getHyperion(ctx.source) ?: return@executes 0
                        hyperion.holdMainHand(ItemArgument.getItem(ctx, "item")
                            .createItemStack(IntegerArgumentType.getInteger(ctx, "count")))
                        1
                    })
                .executes { ctx ->
                    getHyperion(ctx.source)?.holdMainHand(
                        ItemArgument.getItem(ctx, "item").createItemStack(1)); 1
                }))

        .then(Commands.literal("offhand")
            .then(Commands.argument("item", ItemArgument.item(buildCtx))
                .executes { ctx ->
                    getHyperion(ctx.source)?.holdOffHand(
                        ItemArgument.getItem(ctx, "item").createItemStack(1)); 1
                }))

        .then(Commands.literal("wear")
            .then(Commands.argument("item", ItemArgument.item(buildCtx))
                .executes { ctx ->
                    getHyperion(ctx.source)?.autoEquip(
                        ItemArgument.getItem(ctx, "item").createItemStack(1)); 1
                }))

        .then(Commands.literal("equip")
            .then(Commands.argument("slot", StringArgumentType.word())
                .then(Commands.argument("item", ItemArgument.item(buildCtx))
                    .executes { ctx ->
                        val hyperion = getHyperion(ctx.source) ?: return@executes 0
                        val slotStr  = StringArgumentType.getString(ctx, "slot")
                        val slot     = slotFromString(slotStr) ?: run {
                            ctx.source.sendFailure(Component.literal(
                                "Unknown slot '$slotStr'. " +
                                        "Valid: head, chest, legs, feet, mainhand, offhand"))
                            return@executes 0
                        }
                        hyperion.equip(slot, ItemArgument.getItem(ctx, "item").createItemStack(1))
                        1
                    })))

        .then(Commands.literal("clear").executes { ctx ->
            getHyperion(ctx.source)?.clearInventory()
            ctx.source.sendSystemMessage(Component.literal("Hyperion's inventory cleared."))
            1
        })

        .then(Commands.literal("default").executes { ctx ->
            val hyperion = getHyperion(ctx.source) ?: return@executes 0
            hyperion.applyLoadout(HyperionLoadout.Builder(ctx.source.registryAccess()).defaultLoadout)
            ctx.source.sendSystemMessage(Component.literal("Default loadout applied."))
            1
        })
}
