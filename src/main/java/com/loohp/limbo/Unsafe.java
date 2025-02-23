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

package com.loohp.limbo;

import com.loohp.limbo.entity.DataWatcher;
import com.loohp.limbo.entity.Entity;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.world.World;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

import java.lang.reflect.Constructor;

@SuppressWarnings("DeprecatedIsStillUsed")
@Deprecated
public class Unsafe {

    private final Limbo instance;
    private com.loohp.limbo.player.Unsafe playerUnsafe;
    private com.loohp.limbo.world.Unsafe worldUnsafe;

    protected Unsafe(Limbo instance) {
        this.instance = instance;
        try {
            Constructor<com.loohp.limbo.player.Unsafe> playerConstructor = com.loohp.limbo.player.Unsafe.class.getDeclaredConstructor();
            playerConstructor.setAccessible(true);
            playerUnsafe = playerConstructor.newInstance();
            playerConstructor.setAccessible(false);

            Constructor<com.loohp.limbo.world.Unsafe> worldConstructor = com.loohp.limbo.world.Unsafe.class.getDeclaredConstructor();
            worldConstructor.setAccessible(true);
            worldUnsafe = worldConstructor.newInstance();
            worldConstructor.setAccessible(false);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Deprecated
    public void a(Player player, GameMode mode) {
        playerUnsafe.a(player, mode);
    }

    @Deprecated
    public void a(Player player, byte slot) {
        playerUnsafe.a(player, slot);
    }

    @Deprecated
    public void a(Player player, int entityId) {
        playerUnsafe.a(player, entityId);
    }

    @Deprecated
    public void a(World world, Entity entity) {
        worldUnsafe.a(world, entity);
    }

    @Deprecated
    public DataWatcher b(World world, Entity entity) {
        return worldUnsafe.b(world, entity);
    }

    @Deprecated
    public void a(Player player, Location location) {
        playerUnsafe.a(player, location);
    }

    @Deprecated
    public void a(Player player) {
        instance.playersByName.put(player.getName(), player);
        instance.playersByUUID.put(player.getUniqueId(), player);
    }

    @Deprecated
    public void b(Player player) {
        instance.getBossBars().values().forEach(each -> each.hidePlayer(player));
        instance.playersByName.remove(player.getName());
        instance.playersByUUID.remove(player.getUniqueId());
    }

}
