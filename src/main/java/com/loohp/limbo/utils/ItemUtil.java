package com.loohp.limbo.utils;

import com.loohp.limbo.registry.BuiltInRegistries;
import org.geysermc.mcprotocollib.protocol.data.game.item.ItemStack;

public class ItemUtil {
    public static ItemStack from(com.loohp.limbo.inventory.ItemStack itemStack) {
        return new ItemStack(BuiltInRegistries.ITEM_REGISTRY.getId(itemStack.type()), itemStack.amount(), itemStack.components());
    }
}
