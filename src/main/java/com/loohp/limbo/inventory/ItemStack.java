/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loohp.limbo.inventory;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentType;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponentTypes;
import org.geysermc.mcprotocollib.protocol.data.game.item.component.DataComponents;

import java.util.Collections;
import java.util.Objects;

public class ItemStack implements Cloneable {

    public static final ItemStack AIR = new ItemStack(Key.key("minecraft:air"));

    private final Key material;
    private final int amount;
    private final DataComponents components;

    public ItemStack(Key material) {
        this(material, 1);
    }

    public ItemStack(Key material, int amount) {
        this(material, amount, new DataComponents(Collections.emptyMap()));
    }

    public ItemStack(Key material, int amount, DataComponents components) {
        this.material = material;
        this.amount = amount;
        this.components = components;
    }

    @SuppressWarnings("MethodDoesntCallSuperMethod")
    @Override
    public ItemStack clone() {
        return new ItemStack(material, amount, components);
    }

    public Key type() {
        return material;
    }

    public ItemStack type(Key material) {
        return new ItemStack(material, amount, components);
    }

    public int amount() {
        return amount;
    }

    public ItemStack amount(int amount) {
        return new ItemStack(material, amount, components);
    }

    public DataComponents components() {
        return components;
    }

    public ItemStack components(DataComponents components) {
        return new ItemStack(material, amount, components);
    }

    public <T> T component(DataComponentType<T> type) {
        return components.get(type);
    }

    public <T> ItemStack component(DataComponentType<T> type, T value) {
        DataComponents components = components().clone();
        components.put(type, value);
        return components(components);
    }

    public Component displayName() {
        if (type().equals(AIR.type()) || components == null) {
            return null;
        }
        try {
            return component(DataComponentTypes.CUSTOM_NAME);
        } catch (Exception e) {
            return null;
        }
    }

    public ItemStack displayName(Component component) {
        if (type().equals(AIR.type())) {
            return this;
        }
        return component(DataComponentTypes.CUSTOM_NAME, component);
    }

    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItemStack itemStack = (ItemStack) o;
        return amount == itemStack.amount && material.equals(itemStack.material) && Objects.equals(components, itemStack.components);
    }

    public boolean isSimilar(ItemStack stack) {
        return stack != null && material.equals(stack.material) && Objects.equals(components, stack.components);
    }

    @Override
    public int hashCode() {
        return Objects.hash(material, amount, components);
    }

    @Override
    public String toString() {
        return "ItemStack{" +
                "material=" + material +
                ", amount=" + amount +
                ", components=" + components +
                '}';
    }
}
