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

package com.loohp.limbo.player;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandSender;
import com.loohp.limbo.entity.DataWatcher;
import com.loohp.limbo.entity.DataWatcher.WatchableField;
import com.loohp.limbo.entity.DataWatcher.WatchableObjectType;
import com.loohp.limbo.entity.EntityEquipment;
import com.loohp.limbo.entity.LivingEntity;
import com.loohp.limbo.events.inventory.InventoryCloseEvent;
import com.loohp.limbo.events.inventory.InventoryOpenEvent;
import com.loohp.limbo.events.player.PlayerChatEvent;
import com.loohp.limbo.events.player.PlayerTeleportEvent;
import com.loohp.limbo.inventory.Inventory;
import com.loohp.limbo.inventory.InventoryHolder;
import com.loohp.limbo.inventory.InventoryView;
import com.loohp.limbo.inventory.TitledInventory;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.network.ClientConnection;
import com.loohp.limbo.utils.BungeecordAdventureConversionUtils;
import com.loohp.limbo.utils.SoundUtil;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Emitter;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.title.TitlePart;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import org.cloudburstmc.math.vector.Vector3d;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.chat.MessageSignature;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.entity.type.EntityType;
import org.geysermc.mcprotocollib.protocol.data.game.inventory.ContainerType;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundResourcePackPushPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.*;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundEntityPositionSyncPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundMoveEntityPosRotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHeldSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundContainerClosePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.inventory.ClientboundOpenScreenPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSoundPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.title.*;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

public class Player extends LivingEntity implements CommandSender, InventoryHolder {

    public static final String CHAT_DEFAULT_FORMAT = "<%name%> %message%";

    public final ClientConnection clientConnection;
    public final PlayerInteractManager playerInteractManager;

    protected final String username;
    protected GameMode gamemode;
    protected DataWatcher watcher;
    protected byte selectedSlot;
    protected final PlayerInventory playerInventory;
    protected final InventoryView inventoryView;
    private final AtomicInteger containerIdCounter;

    @WatchableField(MetadataIndex = 15, WatchableObjectType = WatchableObjectType.FLOAT)
    protected float additionalHearts = 0.0F;
    @WatchableField(MetadataIndex = 16, WatchableObjectType = WatchableObjectType.VARINT)
    protected int score = 0;
    @WatchableField(MetadataIndex = 17, WatchableObjectType = WatchableObjectType.BYTE)
    protected byte skinLayers = 0;
    @WatchableField(MetadataIndex = 18, WatchableObjectType = WatchableObjectType.BYTE)
    protected byte mainHand = 1;
    //@WatchableField(MetadataIndex = 19, WatchableObjectType = WatchableObjectType.NBT)
    //protected Entity leftShoulder = null;
    //@WatchableField(MetadataIndex = 20, WatchableObjectType = WatchableObjectType.NBT)
    //protected Entity rightShoulder = null;

    public Player(ClientConnection clientConnection, String username, UUID uuid, int entityId, Location location, PlayerInteractManager playerInteractManager) {
        super(EntityType.PLAYER, entityId, uuid, location.getWorld(), location.getX(), location.getY(), location.getZ(), location.getYaw(), location.getPitch());
        this.clientConnection = clientConnection;
        this.username = username;
        this.entityId = entityId;
        this.containerIdCounter = new AtomicInteger(1);
        this.playerInventory = new PlayerInventory(this);
        this.inventoryView = new InventoryView(this, null, null, playerInventory);
        this.playerInteractManager = playerInteractManager;
        this.playerInteractManager.setPlayer(this);
        this.watcher = new DataWatcher(this);
        try {
            this.watcher.update();
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    protected int nextContainerId() {
        return containerIdCounter.updateAndGet(i -> ++i > Byte.MAX_VALUE ? 1 : i);
    }

    public byte getSelectedSlot() {
        return selectedSlot;
    }

    public void setSelectedSlot(byte slot) {
        if (slot == selectedSlot) {
            return;
        }
        ClientboundSetHeldSlotPacket state = new ClientboundSetHeldSlotPacket(slot);
        clientConnection.sendPacket(state);
        this.selectedSlot = slot;
    }

    public GameMode getGamemode() {
        return gamemode;
    }

    public void setGamemode(GameMode gamemode) {
        if (!this.gamemode.equals(gamemode)) {
            ClientboundGameEventPacket state = new ClientboundGameEventPacket(GameEvent.CHANGE_GAMEMODE, gamemode);
            clientConnection.sendPacket(state);
        }
        this.gamemode = gamemode;
    }

    @Deprecated
    protected void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public float getAdditionalHearts() {
        return additionalHearts;
    }

    public void setAdditionalHearts(float additionalHearts) {
        this.additionalHearts = additionalHearts;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public byte getSkinLayers() {
        return skinLayers;
    }

    public void setSkinLayers(byte skinLayers) {
        this.skinLayers = skinLayers;
    }

    public byte getMainHand() {
        return mainHand;
    }

    public void setMainHand(byte mainHand) {
        this.mainHand = mainHand;
    }

    @Override
    public DataWatcher getDataWatcher() {
        return watcher;
    }

    @Override
    public boolean isValid() {
        return Limbo.getInstance().getPlayers().contains(this);
    }

    @Override
    public void remove() {

    }
	
	/*
	public Entity getLeftShoulder() {
		return leftShoulder;
	}

	public void setLeftShoulder(Entity leftShoulder) {
		this.leftShoulder = leftShoulder;
	}

	public Entity getRightShoulder() {
		return rightShoulder;
	}

	public void setRightShoulder(Entity rightShoulder) {
		this.rightShoulder = rightShoulder;
	}
	*/

    @Override
    public String getName() {
        return username;
    }

    @Override
    public boolean hasPermission(String permission) {
        return Limbo.getInstance().getPermissionsManager().hasPermission(this, permission);
    }

    @Override
    public void teleport(Location location) {
        PlayerTeleportEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerTeleportEvent(this, getLocation(), location));
        if (!event.isCancelled()) {
            location = event.getTo();
            super.teleport(location);
            if (!world.equals(location.getWorld())) {
                ClientboundRespawnPacket respawn = new ClientboundRespawnPacket(
                        new PlayerSpawnInfo(
                                location.getWorld().getEnvironment().getId(),
                                Key.key(location.getWorld().getName()),
                                100,
                                getGamemode(),
                                getGamemode(),
                                !Limbo.getInstance().getServerProperties().isReducedDebugInfo(),
                                false,
                                null,
                                100,
                                5
                        ),
                        false, true
                );
                clientConnection.sendPacket(respawn);
            }
            ClientboundPlayerPositionPacket positionLook = new ClientboundPlayerPositionPacket(1, location.getX(), location.getY(), location.getZ(), 0, 0, 0, location.getYaw(), location.getPitch());
            clientConnection.sendPacket(positionLook);
        }
    }

    protected void setLocation(Location location) {
        super.teleport(location);
    }

    public void sendPluginMessage(Key channel, byte[] data) throws IOException {
        sendPluginMessage(channel.toString(), data);
    }

    @Deprecated
    public void sendPluginMessage(String channel, byte[] data) throws IOException {
        clientConnection.sendPluginMessage(channel, data);
    }

    public void sendMessage(String message, UUID uuid) {
        sendMessage(Identity.identity(uuid), LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Deprecated
    public void sendMessage(BaseComponent component, UUID uuid) {
        sendMessage(new BaseComponent[]{component}, uuid);
    }

    @Deprecated
    @Override
    public void sendMessage(BaseComponent[] component, UUID uuid) {
        sendMessage(Identity.identity(uuid), BungeecordAdventureConversionUtils.toComponent(component));
    }

    public void sendMessage(String message) {
        sendMessage(LegacyComponentSerializer.legacySection().deserialize(message));
    }

    @Deprecated
    public void sendMessage(BaseComponent component) {
        sendMessage(new BaseComponent[]{component});
    }

    @Deprecated
    @Override
    public void sendMessage(BaseComponent[] component) {
        sendMessage(BungeecordAdventureConversionUtils.toComponent(component));
    }

    public void disconnect() {
        disconnect(Component.translatable("multiplayer.disconnect.kicked"));
    }

    public void disconnect(String reason) {
        disconnect(LegacyComponentSerializer.legacySection().deserialize(reason));
    }

    public void disconnect(Component reason) {
        clientConnection.disconnect(reason);
    }

    @Deprecated
    public void disconnect(BaseComponent reason) {
        disconnect(new BaseComponent[]{reason});
    }

    @Deprecated
    public void disconnect(BaseComponent[] reason) {
        disconnect(BungeecordAdventureConversionUtils.toComponent(reason));
    }

    public void chat(String message) {
        chat(message, false);
    }

    public void chat(String message, boolean verbose) {
        chat(message, verbose, null, Instant.now());
    }

    public void chat(String message, boolean verbose, MessageSignature saltSignature, Instant time) {
        if (Limbo.getInstance().getServerProperties().isAllowChat()) {
            PlayerChatEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerChatEvent(this, CHAT_DEFAULT_FORMAT, message, false));
            if (!event.isCancelled()) {
                if (hasPermission("limboserver.chat")) {
                    String chat = event.getFormat().replace("%name%", username).replace("%message%", event.getMessage());
                    Limbo.getInstance().getConsole().sendMessage(chat);
                    if (event.getFormat().equals(CHAT_DEFAULT_FORMAT)) {
                        for (Player each : Limbo.getInstance().getPlayers()) {
                            each.sendMessage(Identity.identity(uuid), Component.translatable("chat.type.text").args(Component.text(this.getName()), Component.text(event.getMessage())), MessageType.CHAT, saltSignature, time);
                        }
                    } else {
                        for (Player each : Limbo.getInstance().getPlayers()) {
                            each.sendMessage(Identity.identity(uuid), Component.text(chat), MessageType.SYSTEM, saltSignature, time);
                        }
                    }
                } else if (verbose) {
                    sendMessage(ChatColor.RED + "You do not have permission to chat!");
                }
            }
        }
    }

    public void setResourcePack(String url, String hash, boolean forced) {
        setResourcePack(url, hash, forced, (BaseComponent[]) null);
    }

    @Deprecated
    public void setResourcePack(String url, String hash, boolean forced, BaseComponent promptmessage) {
        setResourcePack(url, hash, forced, promptmessage == null ? null : new BaseComponent[]{promptmessage});
    }

    @Deprecated
    public void setResourcePack(String url, String hash, boolean forced, BaseComponent[] promptmessage) {
        setResourcePack(url, hash, forced, promptmessage == null ? null : BungeecordAdventureConversionUtils.toComponent(promptmessage));
    }

    public void setResourcePack(String url, String hash, boolean forced, Component promptmessage) {
        ClientboundResourcePackPushPacket packsend = new ClientboundResourcePackPushPacket(UUID.randomUUID(), url, hash, forced, promptmessage);
        clientConnection.sendPacket(packsend);
    }

    @Deprecated
    public void setPlayerListHeaderFooter(BaseComponent[] header, BaseComponent[] footer) {
        sendPlayerListHeaderAndFooter(header == null ? Component.empty() : BungeecordAdventureConversionUtils.toComponent(header), footer == null ? Component.empty() : BungeecordAdventureConversionUtils.toComponent(footer));
    }

    @Deprecated
    public void setPlayerListHeaderFooter(BaseComponent header, BaseComponent footer) {
        sendPlayerListHeaderAndFooter(header == null ? Component.empty() : BungeecordAdventureConversionUtils.toComponent(header), footer == null ? Component.empty() : BungeecordAdventureConversionUtils.toComponent(footer));
    }

    public void setPlayerListHeaderFooter(String header, String footer) {
        sendPlayerListHeaderAndFooter(header == null ? Component.empty() : LegacyComponentSerializer.legacySection().deserialize(header), footer == null ? Component.empty() : LegacyComponentSerializer.legacySection().deserialize(footer));
    }

    @Deprecated
    public void setTitle(BaseComponent[] title) {
        sendTitlePart(TitlePart.TITLE, BungeecordAdventureConversionUtils.toComponent(title));
    }

    @Deprecated
    public void setTitle(BaseComponent title) {
        sendTitlePart(TitlePart.TITLE, BungeecordAdventureConversionUtils.toComponent(title));
    }

    public void setTitle(String title) {
        sendTitlePart(TitlePart.TITLE, LegacyComponentSerializer.legacySection().deserialize(title));
    }

    @Deprecated
    public void setSubTitle(BaseComponent[] subTitle) {
        sendTitlePart(TitlePart.SUBTITLE, BungeecordAdventureConversionUtils.toComponent(subTitle));
    }

    @Deprecated
    public void setSubTitle(BaseComponent subTitle) {
        sendTitlePart(TitlePart.SUBTITLE, BungeecordAdventureConversionUtils.toComponent(subTitle));
    }

    public void setSubTitle(String subTitle) {
        sendTitlePart(TitlePart.SUBTITLE, LegacyComponentSerializer.legacySection().deserialize(subTitle));
    }

    public void setTitleTimer(int fadeIn, int stay, int fadeOut) {
        sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ofMillis(fadeIn * 50), Duration.ofMillis(stay * 50), Duration.ofMillis(fadeOut * 50)));
    }

    @Deprecated
    public void setTitleSubTitle(BaseComponent[] title, BaseComponent[] subTitle, int fadeIn, int stay, int fadeOut) {
        setTitleTimer(fadeIn, stay, fadeOut);
        setTitle(title);
        setSubTitle(subTitle);
    }

    @Deprecated
    public void setTitleSubTitle(BaseComponent title, BaseComponent subTitle, int fadeIn, int stay, int fadeOut) {
        setTitleSubTitle(new BaseComponent[]{title}, new BaseComponent[]{subTitle}, fadeIn, stay, fadeOut);
    }

    public void setTitleSubTitle(String title, String subTitle, int fadeIn, int stay, int fadeOut) {
        sendTitlePart(TitlePart.TIMES, Title.Times.times(Duration.ofMillis(fadeIn * 50), Duration.ofMillis(stay * 50), Duration.ofMillis(fadeOut * 50)));
        sendTitlePart(TitlePart.SUBTITLE, LegacyComponentSerializer.legacySection().deserialize(subTitle));
        sendTitlePart(TitlePart.TITLE, LegacyComponentSerializer.legacySection().deserialize(title));
    }

    @Override
    public void sendMessage(Identity source, Component message, MessageType type) {
        sendMessage(source, message, type, null, Instant.now());
    }

    public void sendMessage(Identity source, Component message, MessageType type, MessageSignature signature, Instant time) {
        Packet chat;
        switch (type) {
            case CHAT:
                /*
                if (signature == null) {
                    chat = new ClientboundPlayerChatPacket(Component.empty(), Optional.of(message), 0, uuid, time, SignatureData.NONE);
                } else {
                    chat = new ClientboundPlayerChatPacket(message, Optional.of(message), 0, uuid, time, signature);
                }
                break;
                */
            case SYSTEM:
            default:
                chat = new ClientboundSystemChatPacket(message, false);
                break;
        }
        clientConnection.sendPacket(chat);
    }

    @Override
    public void openBook(Book book) {
        throw new UnsupportedOperationException("This function has not been implemented yet.");
    }

    @Override
    public void stopSound(SoundStop stop) {
        ClientboundStopSoundPacket stopSound = new ClientboundStopSoundPacket(SoundUtil.from(stop.source()), stop.sound());
        clientConnection.sendPacket(stopSound);
    }

    @Override
    public void playSound(Sound sound, Emitter emitter) {
        throw new UnsupportedOperationException("This function has not been implemented yet.");
    }

    @Override
    public void playSound(Sound sound, double x, double y, double z) {
        ClientboundSoundPacket namedSoundEffect = new ClientboundSoundPacket(
                SoundUtil.from(sound.name()),
                SoundUtil.from(sound.source()),
                x, y, z, sound.volume(), sound.pitch(), sound.seed().orElse(ThreadLocalRandom.current().nextLong())
        );
        clientConnection.sendPacket(namedSoundEffect);
    }

    @Override
    public void playSound(Sound sound) {
        playSound(sound, x, y, z);
    }

    @Override
    public void sendActionBar(Component message) {
        ClientboundSetActionBarTextPacket setActionBar = new ClientboundSetActionBarTextPacket(message);
        clientConnection.sendPacket(setActionBar);
    }

    @Override
    public void sendPlayerListHeaderAndFooter(Component header, Component footer) {
        ClientboundTabListPacket listHeaderFooter = new ClientboundTabListPacket(header, footer);
        clientConnection.sendPacket(listHeaderFooter);
    }

    @Override
    public <T> void sendTitlePart(TitlePart<T> part, T value) {
        if (part.equals(TitlePart.TITLE)) {
            ClientboundSetTitleTextPacket setTitle = new ClientboundSetTitleTextPacket((Component) value);
            clientConnection.sendPacket(setTitle);
        } else if (part.equals(TitlePart.SUBTITLE)) {
            ClientboundSetSubtitleTextPacket setSubTitle = new ClientboundSetSubtitleTextPacket((Component) value);
            clientConnection.sendPacket(setSubTitle);
        } else if (part.equals(TitlePart.TIMES)) {
            Times times = (Times) value;
            ClientboundSetTitlesAnimationPacket setSubTitle = new ClientboundSetTitlesAnimationPacket((int) (times.fadeIn().toMillis() / 50), (int) (times.stay().toMillis() / 50), (int) (times.fadeOut().toMillis() / 50));
            clientConnection.sendPacket(setSubTitle);
        }
    }

    @Override
    public void clearTitle() {
        ClientboundClearTitlesPacket clearTitle = new ClientboundClearTitlesPacket(false);
        clientConnection.sendPacket(clearTitle);
    }

    @Override
    public void resetTitle() {
        ClientboundClearTitlesPacket clearTitle = new ClientboundClearTitlesPacket(true);
        clientConnection.sendPacket(clearTitle);
    }

    /**
     * Use {@link com.loohp.limbo.bossbar.KeyedBossBar#showPlayer(Player)} instead
     */
    @Override
    @Deprecated
    public void showBossBar(BossBar bar) {
        Limbo.getInstance().getBossBars().values().stream().filter(each -> each.getProperties() == bar).findFirst().ifPresent(each -> each.showPlayer(this));
    }

    /**
     * Use {@link com.loohp.limbo.bossbar.KeyedBossBar#hidePlayer(Player)} instead
     */
    @Override
    @Deprecated
    public void hideBossBar(BossBar bar) {
        Limbo.getInstance().getBossBars().values().stream().filter(each -> each.getProperties() == bar).findFirst().ifPresent(each -> each.hidePlayer(this));
    }

    @Override
    public PlayerInventory getInventory() {
        return playerInventory;
    }

    public InventoryView getInventoryView() {
        return inventoryView;
    }

    public void updateInventory() {
        playerInventory.updateInventory(this);
    }

    public void openInventory(Inventory inventory) {
        Component title = inventory instanceof TitledInventory ? ((TitledInventory) inventory).getTitle() : Component.translatable("container.chest");
        inventoryView.getUnsafe().a(inventory, title);
        int id = nextContainerId();
        inventory.getUnsafe().c().put(this, id);
        InventoryOpenEvent event = Limbo.getInstance().getEventsManager().callEvent(new InventoryOpenEvent(inventoryView));
        if (event.isCancelled()) {
            inventoryView.getUnsafe().a(null, null);
            inventory.getUnsafe().c().remove(this);
        } else {
            ClientboundOpenScreenPacket packet = new ClientboundOpenScreenPacket(id, inventory.getType().getRawType(inventory.getSize()), title);
            clientConnection.sendPacket(packet);
            inventoryView.updateView();
        }
    }

    public void closeInventory() {
        Inventory inventory = inventoryView.getTopInventory();
        if (inventory != null) {
            Integer id = inventory.getUnsafe().c().get(this);
            if (id != null) {
                Limbo.getInstance().getEventsManager().callEvent(new InventoryCloseEvent(inventoryView));
                inventoryView.getUnsafe().a(null, null);
                inventory.getUnsafe().c().remove(this);
                ClientboundContainerClosePacket packet = new ClientboundContainerClosePacket(id);
                clientConnection.sendPacket(packet);
            }
        }
    }

    public EntityEquipment getEquipment() {
        return playerInventory;
    }

    @Override
    public InventoryHolder getHolder() {
        return this;
    }
}
