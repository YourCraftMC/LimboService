package com.loohp.limbo.utils;

import com.loohp.limbo.registry.BuiltInRegistries;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

public class ItemUtil {
    public static final @Nullable ItemStack AIR = new ItemStack(0, 0);

    public static ItemStack from(com.loohp.limbo.inventory.ItemStack itemStack) {
        if (itemStack == null) {
            return null;
        }
        return new ItemStack(BuiltInRegistries.ITEM_REGISTRY.getId(itemStack.type()), itemStack.amount(), itemStack.components());
    }
}
