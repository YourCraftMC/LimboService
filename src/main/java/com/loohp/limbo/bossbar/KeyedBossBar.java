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

package com.loohp.limbo.bossbar;

import com.loohp.limbo.player.Player;
import com.loohp.limbo.utils.BossBarUtil;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarAction;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarColor;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarDivision;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundBossEventPacket;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

public class KeyedBossBar {

    private final UUID uuid;
    private final Key key;
    private final BossBar properties;
    private final Set<Player> players;
    protected final LimboBossBarHandler listener;
    protected final AtomicBoolean valid;
    private final Unsafe unsafe;

    KeyedBossBar(Key key, BossBar properties) {
        this.uuid = UUID.randomUUID();
        this.key = key;
        this.properties = properties;
        this.players = ConcurrentHashMap.newKeySet();
        this.listener = new LimboBossBarHandler(this);
        this.properties.addListener(listener);
        this.valid = new AtomicBoolean(true);
        this.unsafe = new Unsafe(this);
    }

    public UUID getUniqueId() {
        return uuid;
    }

    public Key getKey() {
        return key;
    }

    public BossBar getProperties() {
        return properties;
    }

    public Set<Player> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public UUID getUuid() {
        return uuid;
    }

    public boolean isValid() {
        return valid.get();
    }

    @Deprecated
    public Unsafe getUnsafe() {
        return unsafe;
    }

    public boolean showPlayer(Player player) {
        ClientboundBossEventPacket packetPlayOutBoss = new ClientboundBossEventPacket(uuid).withAction(BossBarAction.ADD)
                .withTitle(getProperties().name())
                .withHealth(getProperties().progress())
                .withColor(BossBarUtil.color(getProperties().color()))
                .withDivision(BossBarUtil.from(getProperties().overlay()))
                .withDarkenSky(getProperties().hasFlag(BossBar.Flag.DARKEN_SCREEN))
                .withShowFog(getProperties().hasFlag(BossBar.Flag.CREATE_WORLD_FOG))
                .withPlayEndMusic(getProperties().hasFlag(BossBar.Flag.PLAY_BOSS_MUSIC))
                ;
        player.clientConnection.sendPacket(packetPlayOutBoss);
        return players.add(player);
    }

    public boolean hidePlayer(Player player) {
        ClientboundBossEventPacket packetPlayOutBoss = new ClientboundBossEventPacket(getUuid()).withAction(BossBarAction.REMOVE);
        player.clientConnection.sendPacket(packetPlayOutBoss);
        return players.remove(player);
    }

    public static class LimboBossBarHandler implements BossBar.Listener {

        private final KeyedBossBar parent;

        private LimboBossBarHandler(KeyedBossBar parent) {
            this.parent = parent;
        }

        @Override
        public void bossBarNameChanged(@NotNull BossBar bar, @NotNull Component oldName, @NotNull Component newName) {
            ClientboundBossEventPacket packetPlayOutBoss = new ClientboundBossEventPacket(parent.getUuid())
                    .withAction(BossBarAction.UPDATE_TITLE);
            for (Player player : parent.getPlayers()) {
                player.clientConnection.sendPacket(packetPlayOutBoss);
            }
        }

        @Override
        public void bossBarProgressChanged(@NotNull BossBar bar, float oldProgress, float newProgress) {
            ClientboundBossEventPacket packetPlayOutBoss = new ClientboundBossEventPacket(parent.getUuid())
                    .withAction(BossBarAction.UPDATE_HEALTH)
                    .withHealth(newProgress);
            for (Player player : parent.getPlayers()) {
                player.clientConnection.sendPacket(packetPlayOutBoss);
            }
        }

        @Override
        public void bossBarColorChanged(@NotNull BossBar bar, BossBar.@NotNull Color oldColor, BossBar.@NotNull Color newColor) {
            ClientboundBossEventPacket packetPlayOutBoss = new ClientboundBossEventPacket(parent.getUuid())
                    .withAction(BossBarAction.UPDATE_STYLE)
                    .withColor(BossBarUtil.color(newColor))
                    .withDivision(BossBarUtil.from(bar.overlay()));
            for (Player player : parent.getPlayers()) {
                player.clientConnection.sendPacket(packetPlayOutBoss);
            }
        }

        @Override
        public void bossBarOverlayChanged(@NotNull BossBar bar, BossBar.@NotNull Overlay oldOverlay, BossBar.@NotNull Overlay newOverlay) {
            ClientboundBossEventPacket packetPlayOutBoss = new ClientboundBossEventPacket(parent.getUuid())
                    .withAction(BossBarAction.UPDATE_STYLE)
                    .withColor(BossBarUtil.color(bar.color()))
                    .withDivision(BossBarUtil.from(bar.overlay()));
            for (Player player : parent.getPlayers()) {
                player.clientConnection.sendPacket(packetPlayOutBoss);
            }
        }

        @Override
        public void bossBarFlagsChanged(@NotNull BossBar bar, @NotNull Set<BossBar.Flag> flagsAdded, @NotNull Set<BossBar.Flag> flagsRemoved) {
            ClientboundBossEventPacket packetPlayOutBoss = new ClientboundBossEventPacket(parent.getUuid())
                    .withAction(BossBarAction.UPDATE_FLAGS)
                    .withDarkenSky(parent.getProperties().hasFlag(BossBar.Flag.DARKEN_SCREEN))
                    .withShowFog(parent.getProperties().hasFlag(BossBar.Flag.CREATE_WORLD_FOG))
                    .withPlayEndMusic(parent.getProperties().hasFlag(BossBar.Flag.PLAY_BOSS_MUSIC));
            for (Player player : parent.getPlayers()) {
                player.clientConnection.sendPacket(packetPlayOutBoss);
            }
        }

    }

}
