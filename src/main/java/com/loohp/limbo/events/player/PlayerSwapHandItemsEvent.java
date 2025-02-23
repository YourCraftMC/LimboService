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

package com.loohp.limbo.events.player;

import com.loohp.limbo.events.Cancellable;
import com.loohp.limbo.inventory.ItemStack;
import com.loohp.limbo.player.Player;

public class PlayerSwapHandItemsEvent extends PlayerEvent implements Cancellable {

	private boolean cancelled;
	private ItemStack mainHandItem;
	private ItemStack offHandItem;

	public PlayerSwapHandItemsEvent(Player player, ItemStack mainHandItem, ItemStack offHandItem) {
		super(player);
		this.mainHandItem = mainHandItem;
		this.offHandItem = offHandItem;
		this.cancelled = false;
	}

	public ItemStack getMainHandItem() {
		return mainHandItem;
	}

	public void setMainHandItem(ItemStack mainHandItem) {
		this.mainHandItem = mainHandItem;
	}

	public ItemStack getOffHandItem() {
		return offHandItem;
	}

	public void setOffHandItem(ItemStack offHandItem) {
		this.offHandItem = offHandItem;
	}

	@Override
	public void setCancelled(boolean cancelled) {
		this.cancelled = cancelled;
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

}
