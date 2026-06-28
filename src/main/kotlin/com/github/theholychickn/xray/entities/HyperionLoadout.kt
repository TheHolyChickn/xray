package com.github.theholychickn.xray.entities

import net.minecraft.core.RegistryAccess
import net.minecraft.core.component.DataComponents
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.Items
import net.minecraft.world.item.enchantment.Enchantment
import net.minecraft.world.item.enchantment.Enchantments
import net.minecraft.world.item.enchantment.ItemEnchantments


/**
 * Describes Hyperion's complete equipment and inventory at spawn time.
 *
 * Apply with [HyperionEntity.applyLoadout] or [applyTo] directly.
 * All stacks are deep-copied on apply so the loadout object is reusable.
 *
 * @param helmet     Head slot item.
 * @param chestplate Chest slot item.
 * @param leggings   Legs slot item.
 * @param boots      Feet slot item.
 * @param mainHand   Main hand item.
 * @param offHand    Offhand item.
 * @param inventory  Up to 27 items placed in [HyperionEntity.inventory] in order.
 */
data class HyperionLoadout(
    val helmet:     ItemStack = ItemStack.EMPTY,
    val chestplate: ItemStack = ItemStack.EMPTY,
    val legs:       ItemStack = ItemStack.EMPTY,
    val boots:      ItemStack = ItemStack.EMPTY,
    val mainHand:   ItemStack = ItemStack.EMPTY,
    val offHand:    ItemStack = ItemStack.EMPTY,
    val inventory:  List<ItemStack> = emptyList(),
) {

    /**
     * Call on a [HyperionLoadout] to apply the loadout to a [HyperionEntity].
     */
    fun applyTo(entity: HyperionEntity) {
        entity.setItemSlot(EquipmentSlot.HEAD, helmet)
        entity.setItemSlot(EquipmentSlot.CHEST, chestplate)
        entity.setItemSlot(EquipmentSlot.LEGS, legs)
        entity.setItemSlot(EquipmentSlot.FEET, boots)
        entity.setItemSlot(EquipmentSlot.MAINHAND, mainHand)
        entity.setItemSlot(EquipmentSlot.OFFHAND, offHand)
        inventory.forEachIndexed { i, stack ->
            if (i < entity.inventory.containerSize) entity.inventory.setItem(i, stack.copy())
        }
    }

    /**
     * Used to construct HyperionLoadouts to be sent to an entity.
     * Requires a [RegistryAccess] to resolve enchantment holders, so always call
     *
     * @property defaultLoadout The default loadout of Hyperion. Contains some useful max items,
     * utility items, and ender chests for his other items.
     * @property maxHelmet A max netherite helmet.
     * @property maxChestplate A max netherite chestplate.
     * @property maxLegs Max netherite leggings.
     * @property maxBoots Max netherite boots.
     * @property maxSword A max netherite sword.
     * @property fortunePick A max netherite fortune pickaxe.
     * @property silkPick A max netherite silk tuoch pickaxe.
     */
    class Builder(val registryAccess: RegistryAccess) {
        /**
         * Creates an enchanted [ItemStack] from [item] using the given [enchants].
         */
        fun enchant(item: Item, vararg enchants: Pair<ResourceKey<Enchantment>, Int>): ItemStack {
            val reg = registryAccess.lookupOrThrow(Registries.ENCHANTMENT)
            val stack = ItemStack(item)
            val mutable = ItemEnchantments.Mutable(ItemEnchantments.EMPTY)
            enchants.forEach { (key, level) ->
                reg.get(key).ifPresent { mutable.set(it, level) }
            }
            stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable())
            return stack
        }

        /**
         * A netherite helmet with prot 4, unbreaking 3, mending, aqua, and resp 3.
         */
        val maxHelmet: ItemStack = enchant(
            Items.NETHERITE_HELMET,
            Enchantments.PROTECTION to 4,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1,
            Enchantments.AQUA_AFFINITY          to 1,
            Enchantments.RESPIRATION            to 3
        )

        /**
         * A netherite chestplate with prot 4, unbreaking 3, and mending.
         */
        val maxChestplate: ItemStack = enchant(
            Items.NETHERITE_CHESTPLATE,
            Enchantments.PROTECTION to 4,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1
        )

        /**
         * A pair of netherite leggings with prot 4, unbreaking 3, mending, and swift sneak 3.
         */
        val maxLegs: ItemStack = enchant(
            Items.NETHERITE_LEGGINGS,
            Enchantments.PROTECTION to 4,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1,
            Enchantments.SWIFT_SNEAK            to 3,
        )

        /**
         * A pair of netherite boots with prot 4, unbreaking 3, mending, feather falling 4, depth strider 3, and
         * soul speed 3.
         */
        val maxBoots: ItemStack = enchant(
            Items.NETHERITE_BOOTS,
            Enchantments.PROTECTION to 4,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1,
            Enchantments.FEATHER_FALLING        to 4,
            Enchantments.DEPTH_STRIDER          to 3,
            Enchantments.SOUL_SPEED             to 3
        )

        /**
         * A netherite sword with sharpness 5, unbreaking 3, mending, looting 3, sweeping edge 3, and fire aspect 2.
         */
        val maxSword: ItemStack = enchant(
            Items.NETHERITE_SWORD,
            Enchantments.SHARPNESS  to 5,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1,
            Enchantments.LOOTING                to 3,
            Enchantments.SWEEPING_EDGE          to 3,
            Enchantments.FIRE_ASPECT            to 2
        )

        /**
         * A netherite pickaxe with efficiency 5, fortune 3, unbreaking 3, and mending.
         */
        val fortunePick: ItemStack = enchant(Items.NETHERITE_PICKAXE,
            Enchantments.EFFICIENCY to 5,
            Enchantments.FORTUNE                to 3,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1
        )

        /**
         * A netherite pickaxe with efficiency 5, fortune 3, unbreaking 3, and mending.
         */
        val silkPick: ItemStack = enchant(Items.NETHERITE_PICKAXE,
            Enchantments.EFFICIENCY to 5,
            Enchantments.SILK_TOUCH             to 1,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1
        )

        /**
         * A netherite shovel with efficiency 5, fortune 3, unbreaking 3, and mending.
         */
        val fortuneShovel: ItemStack = enchant(Items.NETHERITE_SHOVEL,
        Enchantments.EFFICIENCY to 5,
        Enchantments.FORTUNE                to 3,
        Enchantments.UNBREAKING             to 3,
        Enchantments.MENDING                to 1
        )

        /**
         * A netherite shovel with efficiency 5, silk touch, unbreaking 3, and mending.
         */
        val silkShovel: ItemStack = enchant(Items.NETHERITE_SHOVEL,
            Enchantments.EFFICIENCY to 5,
            Enchantments.SILK_TOUCH             to 1,
            Enchantments.UNBREAKING             to 3,
            Enchantments.MENDING                to 1
        )

        val defaultLoadout: HyperionLoadout = HyperionLoadout(
            helmet = maxHelmet,
            chestplate = maxChestplate,
            legs = maxLegs,
            boots = maxBoots,
            mainHand = maxSword,
            offHand = ItemStack(Items.TOTEM_OF_UNDYING),
            inventory = listOf(
                // Tools
                fortunePick,
                silkPick,
                silkShovel,
                // Elytra (for flight when not using commands)
                enchant(Items.ELYTRA,
                    Enchantments.UNBREAKING to 3,
                    Enchantments.MENDING                to 1
                ),
                // Storage
                ItemStack(Items.ENDER_CHEST, 64),
                ItemStack(Items.SHULKER_BOX),
                ItemStack(Items.SHULKER_BOX),
                ItemStack(Items.SHULKER_BOX),
                ItemStack(Items.SHULKER_BOX),
                // Consumables
                ItemStack(Items.TOTEM_OF_UNDYING, 3),
                ItemStack(Items.ENCHANTED_GOLDEN_APPLE, 4),
                ItemStack(Items.GOLDEN_APPLE, 16),
                ItemStack(Items.CHORUS_FRUIT, 16),
                ItemStack(Items.ENDER_PEARL, 16),
                // Utilities
                ItemStack(Items.WATER_BUCKET),
                ItemStack(Items.FLINT_AND_STEEL),
                ItemStack(Items.FIREWORK_ROCKET, 64),
                // Books and writing
                ItemStack(Items.WRITABLE_BOOK, 8),
                ItemStack(Items.INK_SAC, 16),
                ItemStack(Items.FEATHER, 8),
            )
        )
    }
}
