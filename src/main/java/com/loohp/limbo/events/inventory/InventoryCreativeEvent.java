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

package com.loohp.limbo.events.inventory;

import com.loohp.limbo.events.Cancellable;
import com.loohp.limbo.inventory.InventoryView;
import com.loohp.limbo.inventory.ItemStack;

public class InventoryCreativeEvent extends InventoryEvent implements Cancellable {

    private boolean cancelled;
    private final int slot;
    private ItemStack newItem;

    public InventoryCreativeEvent(InventoryView inventoryView, int slot, ItemStack newItem) {
        super(inventoryView, inventoryView.getBottomInventory());
        this.slot = slot;
        this.newItem = newItem;
        this.cancelled = false;
    }

    public int getSlot() {
        return slot;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    public ItemStack getNewItem() {
        return newItem == null ? null : newItem.clone();
    }

    public void setNewItem(ItemStack newItem) {
        this.newItem = newItem == null ? null : newItem.clone();
    }
}
