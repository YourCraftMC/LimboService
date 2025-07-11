/*
  ~ This file is part of Limbo.
  ~
  ~ Copyright (C) 2024. YourCraftMC <admin@ycraft.cn>
  ~ Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
  ~ Copyright (C) 2022. Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
 */

package com.loohp.limbo.inventory;

import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;

public enum InventoryType {

    /**
     * A chest inventory, with 0, 9, 18, 27, 36, 45, or 54 slots of type
     * CONTAINER.
     */
    CHEST(27, "Chest"),
    /**
     * A dispenser inventory, with 9 slots of type CONTAINER.
     */
    DISPENSER(9, "Dispenser"),
    /**
     * A dropper inventory, with 9 slots of type CONTAINER.
     */
    DROPPER(9, "Dropper"),
    /**
     * A furnace inventory, with a RESULT slot, a CRAFTING slot, and a FUEL
     * slot.
     */
    FURNACE(3, "Furnace"),
    /**
     * A workbench inventory, with 9 CRAFTING slots and a RESULT slot.
     */
    WORKBENCH(10, "Crafting"),
    /**
     * A player's crafting inventory, with 4 CRAFTING slots and a RESULT slot.
     * Also implies that the 4 ARMOR slots are accessible.
     */
    CRAFTING(5, "Crafting", false),
    /**
     * An enchantment table inventory, with two CRAFTING slots and three
     * enchanting buttons.
     */
    ENCHANTING(2, "Enchanting"),
    /**
     * A brewing stand inventory, with one FUEL slot and four CRAFTING slots.
     */
    BREWING(5, "Brewing"),
    /**
     * A player's inventory, with 9 QUICKBAR slots, 27 CONTAINER slots, 4 ARMOR
     * slots and 1 offhand slot. The ARMOR and offhand slots may not be visible
     * to the player, though.
     */
    PLAYER(41, "Player"),
    /**
     * The creative mode inventory, with only 9 QUICKBAR slots and nothing
     * else. (The actual creative interface with the items is client-side and
     * cannot be altered by the server.)
     */
    CREATIVE(9, "Creative", false),
    /**
     * The merchant inventory, with 2 CRAFTING slots, and 1 RESULT slot.
     */
    MERCHANT(3, "Villager", false),
    /**
     * The ender chest inventory, with 27 slots.
     */
    ENDER_CHEST(27, "Ender Chest"),
    /**
     * An anvil inventory, with 2 CRAFTING slots and 1 RESULT slot
     */
    ANVIL(3, "Repairing"),
    /**
     * A smithing inventory, with 2 CRAFTING slots and 1 RESULT slot
     */
    SMITHING(3, "Upgrade Gear"),
    /**
     * A beacon inventory, with 1 CRAFTING slot
     */
    BEACON(1, "container.beacon"),
    /**
     * A hopper inventory, with 5 slots of type CONTAINER.
     */
    HOPPER(5, "Item Hopper"),
    /**
     * A shulker box inventory, with 27 slots of type CONTAINER.
     */
    SHULKER_BOX(27, "Shulker Box"),
    /**
     * A barrel box inventory, with 27 slots of type CONTAINER.
     */
    BARREL(27, "Barrel"),
    /**
     * A blast furnace inventory, with a RESULT slot, a CRAFTING slot, and a
     * FUEL slot.
     */
    BLAST_FURNACE(3, "Blast Furnace"),
    /**
     * A lectern inventory, with 1 BOOK slot.
     */
    LECTERN(1, "Lectern"),
    /**
     * A smoker inventory, with a RESULT slot, a CRAFTING slot, and a FUEL slot.
     */
    SMOKER(3, "Smoker"),
    /**
     * Loom inventory, with 3 CRAFTING slots, and 1 RESULT slot.
     */
    LOOM(4, "Loom"),
    /**
     * Cartography inventory with 2 CRAFTING slots, and 1 RESULT slot.
     */
    CARTOGRAPHY(3, "Cartography Table"),
    /**
     * Grindstone inventory with 2 CRAFTING slots, and 1 RESULT slot.
     */
    GRINDSTONE(3, "Repair & Disenchant"),
    /**
     * Stonecutter inventory with 1 CRAFTING slot, and 1 RESULT slot.
     */
    STONECUTTER(2, "Stonecutter"),
    /**
     * Pseudo composter inventory with 0 or 1 slots of undefined type.
     */
    COMPOSTER(1, "Composter"),
    /**
     * Pseudo chiseled bookshelf inventory, with 6 slots of undefined type.
     */
    CHISELED_BOOKSHELF(6, "Chiseled Bookshelf"),
    ;

    private final int size;
    private final String title;
    private final boolean isCreatable;

    InventoryType(int defaultSize, /*@NotNull*/ String defaultTitle) {
        this(defaultSize, defaultTitle, true);
    }

    InventoryType(int defaultSize, /*@NotNull*/ String defaultTitle, boolean isCreatable) {
        size = defaultSize;
        title = defaultTitle;
        this.isCreatable = isCreatable;
    }

    public int getDefaultSize() {
        return size;
    }

    public String getDefaultTitle() {
        return title;
    }

    /**
     * Denotes that this InventoryType can be created via the normal
     * {@link org.bukkit.Bukkit#createInventory} methods.
     *
     * @return if this InventoryType can be created and shown to a player
     */
    public boolean isCreatable() {
        return isCreatable;
    }

    public ContainerType getRawType(int slots) {
        switch (this) {
            case CHEST:
                return ContainerType.valueOf("GENERIC_9X" + (slots / 9));
            case DISPENSER:
            case DROPPER:
                return ContainerType.GENERIC_3X3;
            case FURNACE:
                return ContainerType.FURNACE;
            case WORKBENCH:
                return ContainerType.CRAFTER_3x3;
            case ENCHANTING:
                return ContainerType.ENCHANTMENT;
            case BREWING:
                return ContainerType.BREWING_STAND;
            case MERCHANT:
                return ContainerType.MERCHANT;
            case ENDER_CHEST:
            case BARREL:
                return ContainerType.GENERIC_9X3;
            case ANVIL:
                return ContainerType.ANVIL;
            case SMITHING:
                return ContainerType.SMITHING;
            case BEACON:
                return ContainerType.BEACON;
            case HOPPER:
                return ContainerType.HOPPER;
            case SHULKER_BOX:
                return ContainerType.SHULKER_BOX;
            case BLAST_FURNACE:
                return ContainerType.BLAST_FURNACE;
            case LECTERN:
                return ContainerType.LECTERN;
            case SMOKER:
                return ContainerType.SMOKER;
            case LOOM:
                return ContainerType.LOOM;
            case CARTOGRAPHY:
                return ContainerType.CARTOGRAPHY;
            case GRINDSTONE:
                return ContainerType.GRINDSTONE;
            case STONECUTTER:
                return ContainerType.STONECUTTER;
            case CRAFTING:
                return ContainerType.CRAFTING;
            case PLAYER:
            case CREATIVE:
            case COMPOSTER:
            case CHISELED_BOOKSHELF:
                return null;
        }
        return null;
    }

    public enum SlotType {
        /**
         * A result slot in a furnace or crafting inventory.
         */
        RESULT,
        /**
         * A slot in the crafting matrix, or an 'input' slot.
         */
        CRAFTING,
        /**
         * An armour slot in the player's inventory.
         */
        ARMOR,
        /**
         * A regular slot in the container or the player's inventory; anything
         * not covered by the other enum values.
         */
        CONTAINER,
        /**
         * A slot in the bottom row or quickbar.
         */
        QUICKBAR,
        /**
         * A pseudo-slot representing the area outside the inventory window.
         */
        OUTSIDE,
        /**
         * The fuel slot in a furnace inventory, or the ingredient slot in a
         * brewing stand inventory.
         */
        FUEL
    }
}
