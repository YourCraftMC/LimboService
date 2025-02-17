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

package com.loohp.limbo.network;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.entity.EntityEquipment;
import com.loohp.limbo.events.inventory.AnvilRenameInputEvent;
import com.loohp.limbo.events.inventory.InventoryCloseEvent;
import com.loohp.limbo.events.inventory.InventoryCreativeEvent;
import com.loohp.limbo.events.player.PlayerInteractEvent;
import com.loohp.limbo.events.player.PlayerJoinEvent;
import com.loohp.limbo.events.player.PlayerMoveEvent;
import com.loohp.limbo.events.player.PlayerQuitEvent;
import com.loohp.limbo.events.player.PlayerResourcePackStatusEvent;
import com.loohp.limbo.events.player.PlayerSelectedSlotChangeEvent;
import com.loohp.limbo.events.player.PlayerSpawnEvent;
import com.loohp.limbo.events.player.PlayerSwapHandItemsEvent;
import com.loohp.limbo.events.player.PluginMessageEvent;
import com.loohp.limbo.file.ServerProperties;
import com.loohp.limbo.inventory.AnvilInventory;
import com.loohp.limbo.inventory.Inventory;
import com.loohp.limbo.inventory.ItemStack;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.network.protocol.packets.ClientboundFinishConfigurationPacket;
import com.loohp.limbo.network.protocol.packets.ClientboundRegistryDataPacket;
import com.loohp.limbo.network.protocol.packets.PacketHandshakingIn;
import com.loohp.limbo.network.protocol.packets.PacketIn;
import com.loohp.limbo.network.protocol.packets.PacketLoginInLoginStart;
import com.loohp.limbo.network.protocol.packets.PacketLoginInPluginMessaging;
import com.loohp.limbo.network.protocol.packets.PacketLoginOutDisconnect;
import com.loohp.limbo.network.protocol.packets.PacketLoginOutLoginSuccess;
import com.loohp.limbo.network.protocol.packets.PacketLoginOutPluginMessaging;
import com.loohp.limbo.network.protocol.packets.PacketOut;
import com.loohp.limbo.network.protocol.packets.PacketPlayInBlockDig;
import com.loohp.limbo.network.protocol.packets.PacketPlayInBlockPlace;
import com.loohp.limbo.network.protocol.packets.PacketPlayInChat;
import com.loohp.limbo.network.protocol.packets.PacketPlayInCloseWindow;
import com.loohp.limbo.network.protocol.packets.PacketPlayInHeldItemChange;
import com.loohp.limbo.network.protocol.packets.PacketPlayInItemName;
import com.loohp.limbo.network.protocol.packets.PacketPlayInKeepAlive;
import com.loohp.limbo.network.protocol.packets.PacketPlayInPickItem;
import com.loohp.limbo.network.protocol.packets.PacketPlayInPluginMessaging;
import com.loohp.limbo.network.protocol.packets.PacketPlayInPosition;
import com.loohp.limbo.network.protocol.packets.PacketPlayInPositionAndLook;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutGameStateChange;
import com.loohp.limbo.network.protocol.packets.ServerboundResourcePackPacket;
import com.loohp.limbo.network.protocol.packets.ServerboundResourcePackPacket.Action;
import com.loohp.limbo.network.protocol.packets.PacketPlayInRotation;
import com.loohp.limbo.network.protocol.packets.PacketPlayInSetCreativeSlot;
import com.loohp.limbo.network.protocol.packets.PacketPlayInTabComplete;
import com.loohp.limbo.network.protocol.packets.PacketPlayInUseItem;
import com.loohp.limbo.network.protocol.packets.PacketPlayInWindowClick;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutDeclareCommands;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutDisconnect;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutEntityMetadata;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutHeldItemChange;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutKeepAlive;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutLogin;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPlayerAbilities;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPlayerAbilities.PlayerAbilityFlags;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPlayerInfo;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPlayerInfo.PlayerInfoAction;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPlayerInfo.PlayerInfoData;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPlayerInfo.PlayerInfoData.PlayerInfoDataAddPlayer.PlayerSkinProperty;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPluginMessaging;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutPositionAndLook;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutSpawnPosition;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutTabComplete;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutTabComplete.TabCompleteMatches;
import com.loohp.limbo.network.protocol.packets.PacketPlayOutUpdateViewPosition;
import com.loohp.limbo.network.protocol.packets.PacketStatusInPing;
import com.loohp.limbo.network.protocol.packets.PacketStatusInRequest;
import com.loohp.limbo.network.protocol.packets.PacketStatusOutPong;
import com.loohp.limbo.network.protocol.packets.PacketStatusOutResponse;
import com.loohp.limbo.network.protocol.packets.ServerboundChatCommandPacket;
import com.loohp.limbo.network.protocol.packets.ServerboundFinishConfigurationPacket;
import com.loohp.limbo.network.protocol.packets.ServerboundLoginAcknowledgedPacket;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.player.PlayerInventory;
import com.loohp.limbo.utils.BungeecordAdventureConversionUtils;
import com.loohp.limbo.utils.CheckedBiConsumer;
import com.loohp.limbo.utils.CustomStringUtils;
import com.loohp.limbo.utils.DataTypeIO;
import com.loohp.limbo.utils.DeclareCommands;
import com.loohp.limbo.utils.InventoryClickUtils;
import com.loohp.limbo.utils.MojangAPIUtils;
import com.loohp.limbo.utils.MojangAPIUtils.SkinResponse;
import com.loohp.limbo.world.BlockPosition;
import com.loohp.limbo.world.BlockState;
import com.loohp.limbo.world.World;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TranslatableComponent;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class ClientConnection extends SessionAdapter {

    private static final Key DEFAULT_HANDLER_NAMESPACE = Key.key("default");
    private static final String BRAND_ANNOUNCE_CHANNEL = Key.key("brand").toString();

    private final Random random = new Random();
    private final Session session;
    private boolean running;

    private Player player;
    private TimerTask keepAliveTask;
    private AtomicLong lastPacketTimestamp;
    private AtomicLong lastKeepAlivePayLoad;
    private SocketAddress inetAddress;
    private boolean ready;

    public ClientConnection(Session session) {
        this.session = session;
        this.inetAddress = session.getLocalAddress();
        this.lastPacketTimestamp = new AtomicLong(-1);
        this.lastKeepAlivePayLoad = new AtomicLong(-1);
        this.running = false;
        this.ready = false;
    }

    public long getLastKeepAlivePayLoad() {
        return lastKeepAlivePayLoad.get();
    }

    public void setLastKeepAlivePayLoad(long payLoad) {
        this.lastKeepAlivePayLoad.set(payLoad);
    }

    public long getLastPacketTimestamp() {
        return lastPacketTimestamp.get();
    }

    public void setLastPacketTimestamp(long payLoad) {
        this.lastPacketTimestamp.set(payLoad);
    }

    public TimerTask getKeepAliveTask() {
        return this.keepAliveTask;
    }

    public Player getPlayer() {
        return player;
    }


    public Session getSession() {
        return session;
    }

    public boolean isRunning() {
        return running;
    }

    public boolean isReady() {
        return ready;
    }

    public void sendPluginMessage(String channel, byte[] data) throws IOException {
        sendPacket(new ClientboundCustomPayloadPacket(Key.key(channel), data));
    }

    public synchronized void sendPacket(Packet packet) throws IOException {
        session.send(packet);
        setLastPacketTimestamp(System.currentTimeMillis());
    }

    public void disconnect(BaseComponent[] reason) {
        disconnect(BungeecordAdventureConversionUtils.toComponent(reason));
    }

    public void disconnect(Component reason) {
        session.disconnect(reason);
    }

    private void disconnectDuringLogin(BaseComponent[] reason) {
        disconnectDuringLogin(BungeecordAdventureConversionUtils.toComponent(reason));
    }

    private void disconnectDuringLogin(Component reason) {
        ServerProperties properties = Limbo.getInstance().getServerProperties();
        if (!properties.isReducedDebugInfo()) {
            String str = (properties.isLogPlayerIPAddresses() ? ((InetSocketAddress) inetAddress).getHostName() : "<ip address withheld>") + ":" + ((InetSocketAddress) session.getLocalAddress()).getPort();
            Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player disconnected with the reason " + PlainTextComponentSerializer.plainText().serialize(reason));
        }
        session.disconnect(reason);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void run() {
        running = true;
        try {
            if (state == ClientState.CONFIGURATION) {

                TimeUnit.MILLISECONDS.sleep(500);

                ServerboundFinishConfigurationPacket serverboundFinishConfigurationPacket = (ServerboundFinishConfigurationPacket) channel.readPacket();

                state = ClientState.PLAY;
                Limbo.getInstance().getUnsafe().a(player);

                TimeUnit.MILLISECONDS.sleep(500);

                ServerProperties properties = Limbo.getInstance().getServerProperties();
                Location worldSpawn = properties.getWorldSpawn();

                PlayerSpawnEvent spawnEvent = Limbo.getInstance().getEventsManager().callEvent(new PlayerSpawnEvent(player, worldSpawn));
                worldSpawn = spawnEvent.getSpawnLocation();
                World world = worldSpawn.getWorld();

                PacketPlayOutLogin join = new PacketPlayOutLogin(player.getEntityId(), false, Limbo.getInstance().getWorlds(), properties.getMaxPlayers(), 8, 8, properties.isReducedDebugInfo(), true, false, world.getEnvironment(), world, 0, properties.getDefaultGamemode(), false, true, 0, 0, false);
                sendPacket(join);
                Limbo.getInstance().getUnsafe().a(player, properties.getDefaultGamemode());

                ByteArrayOutputStream brandOut = new ByteArrayOutputStream();
                DataTypeIO.writeString(new DataOutputStream(brandOut), properties.getServerModName(), StandardCharsets.UTF_8);
                sendPluginMessage(BRAND_ANNOUNCE_CHANNEL, brandOut.toByteArray());

                SkinResponse skinresponce = (isVelocityModern || isBungeeGuard || isBungeecord) && forwardedSkin != null ? forwardedSkin : MojangAPIUtils.getSkinFromMojangServer(player.getName());
                PlayerSkinProperty skin = skinresponce != null ? new PlayerSkinProperty(skinresponce.getSkin(), skinresponce.getSignature()) : null;
                PacketPlayOutPlayerInfo info = new PacketPlayOutPlayerInfo(EnumSet.of(PlayerInfoAction.ADD_PLAYER, PlayerInfoAction.UPDATE_GAME_MODE, PlayerInfoAction.UPDATE_LISTED, PlayerInfoAction.UPDATE_LATENCY, PlayerInfoAction.UPDATE_DISPLAY_NAME), player.getUniqueId(), new PlayerInfoData.PlayerInfoDataAddPlayer(player.getName(), true, Optional.ofNullable(skin), properties.getDefaultGamemode(), 0, false, Optional.empty()));
                sendPacket(info);

                Set<PlayerAbilityFlags> flags = new HashSet<>();
                if (properties.isAllowFlight()) {
                    flags.add(PlayerAbilityFlags.FLY);
                }
                if (player.getGamemode().equals(GameMode.CREATIVE)) {
                    flags.add(PlayerAbilityFlags.CREATIVE);
                }
                PacketPlayOutPlayerAbilities abilities = new PacketPlayOutPlayerAbilities(0.05F, 0.1F, flags.toArray(new PlayerAbilityFlags[flags.size()]));
                sendPacket(abilities);

                String str = (properties.isLogPlayerIPAddresses() ? inetAddress.getHostName() : "<ip address withheld>") + ":" + session.getPort() + "|" + player.getName() + "(" + player.getUniqueId() + ")";
                Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player had connected to the Limbo server!");

                PacketPlayOutGameStateChange gameEvent = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.GameStateChangeEvent.LEVEL_CHUNKS_LOAD_START, 0);
                sendPacket(gameEvent);
                player.playerInteractManager.update();

                PacketPlayOutDeclareCommands declare = DeclareCommands.getDeclareCommandsPacket(player);
                if (declare != null) {
                    sendPacket(declare);
                }

                PacketPlayOutSpawnPosition spawnPos = new PacketPlayOutSpawnPosition(BlockPosition.from(worldSpawn), worldSpawn.getPitch());
                sendPacket(spawnPos);

                PacketPlayOutPositionAndLook positionLook = new PacketPlayOutPositionAndLook(worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ(), worldSpawn.getYaw(), worldSpawn.getPitch(), 1);
                Limbo.getInstance().getUnsafe().a(player, new Location(world, worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ(), worldSpawn.getYaw(), worldSpawn.getPitch()));
                sendPacket(positionLook);

                player.getDataWatcher().update();
                PacketPlayOutEntityMetadata show = new PacketPlayOutEntityMetadata(player, false, Player.class.getDeclaredField("skinLayers"));
                sendPacket(show);

                Limbo.getInstance().getEventsManager().callEvent(new PlayerJoinEvent(player));

                if (properties.isAllowFlight()) {
                    PacketPlayOutGameStateChange state = new PacketPlayOutGameStateChange(PacketPlayOutGameStateChange.GameStateChangeEvent.CHANGE_GAME_MODE, player.getGamemode().getId());
                    sendPacket(state);
                }

                // RESOURCEPACK CODE CONRIBUTED BY GAMERDUCK123
                if (!properties.getResourcePackLink().equalsIgnoreCase("")) {
                    if (!properties.getResourcePackSHA1().equalsIgnoreCase("")) {
                        //SEND RESOURCEPACK
                        player.setResourcePack(properties.getResourcePackLink(), properties.getResourcePackSHA1(), properties.getResourcePackRequired(), properties.getResourcePackPrompt());
                    } else {
                        //NO SHA
                        Limbo.getInstance().getConsole().sendMessage("ResourcePacks require SHA1s");
                    }
                } else {
                    //RESOURCEPACK NOT ENABLED
                }

                // PLAYER LIST HEADER AND FOOTER CODE CONRIBUTED BY GAMERDUCK123
                player.sendPlayerListHeaderAndFooter(properties.getTabHeader(), properties.getTabFooter());

                ready = true;

                keepAliveTask = new TimerTask() {
                    @Override
                    public void run() {
                        if (state.equals(ClientState.DISCONNECTED)) {
                            this.cancel();
                        } else if (ready && state.equals(ClientState.PLAY)) {
                            long now = System.currentTimeMillis();
                            if (now - getLastPacketTimestamp() > 15000) {
                                PacketPlayOutKeepAlive keepAlivePacket = new PacketPlayOutKeepAlive(now);
                                try {
                                    sendPacket(keepAlivePacket);
                                    setLastKeepAlivePayLoad(now);
                                } catch (Exception e) {
                                }
                            }
                        }
                    }
                };
                new Timer().schedule(keepAliveTask, 5000, 10000);

                while (session.isConnected()) {
                    try {
                        CheckedBiConsumer<PlayerMoveEvent, Location, IOException> processMoveEvent = (event, originalTo) -> {
                            if (event.isCancelled()) {
                                Location returnTo = event.getFrom();
                                PacketPlayOutPositionAndLook cancel = new PacketPlayOutPositionAndLook(returnTo.getX(), returnTo.getY(), returnTo.getZ(), returnTo.getYaw(), returnTo.getPitch(), 1);
                                sendPacket(cancel);
                            } else {
                                Location to = event.getTo();
                                Limbo.getInstance().getUnsafe().a(player, to);
                                // If an event handler used setTo, let's make sure we tell the player about it.
                                if (!originalTo.equals(to)) {
                                    PacketPlayOutPositionAndLook pos = new PacketPlayOutPositionAndLook(to.getX(), to.getY(), to.getZ(), to.getYaw(), to.getPitch(), 1);
                                    sendPacket(pos);
                                }
                                PacketPlayOutUpdateViewPosition response = new PacketPlayOutUpdateViewPosition((int) player.getLocation().getX() >> 4, (int) player.getLocation().getZ() >> 4);
                                sendPacket(response);
                            }
                        };

                        PacketIn packetIn = channel.readPacket();

                        if (packetIn instanceof PacketPlayInPositionAndLook) {
                            PacketPlayInPositionAndLook pos = (PacketPlayInPositionAndLook) packetIn;
                            Location from = player.getLocation();
                            Location to = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());

                            if (!from.equals(to)) {
                                PlayerMoveEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerMoveEvent(player, from, to));
                                processMoveEvent.consume(event, to);
                            }
                        } else if (packetIn instanceof PacketPlayInPosition) {
                            PacketPlayInPosition pos = (PacketPlayInPosition) packetIn;
                            Location from = player.getLocation();
                            Location to = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

                            if (!from.equals(to)) {
                                PlayerMoveEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerMoveEvent(player, from, to));
                                processMoveEvent.consume(event, to);
                            }
                        } else if (packetIn instanceof PacketPlayInRotation) {
                            PacketPlayInRotation pos = (PacketPlayInRotation) packetIn;
                            Location from = player.getLocation();
                            Location to = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), pos.getYaw(), pos.getPitch());

                            if (!from.equals(to)) {
                                PlayerMoveEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerMoveEvent(player, from, to));
                                processMoveEvent.consume(event, to);
                            }
                        } else if (packetIn instanceof PacketPlayInKeepAlive) {
                            long lastPayload = getLastKeepAlivePayLoad();
                            PacketPlayInKeepAlive alive = (PacketPlayInKeepAlive) packetIn;
                            if (lastPayload == -1) {
                                Limbo.getInstance().getConsole().sendMessage("Unsolicited KeepAlive packet for player " + player.getName());
                            } else if (alive.getPayload() != lastPayload) {
                                Limbo.getInstance().getConsole().sendMessage("Incorrect Payload received in KeepAlive packet for player " + player.getName());
                                break;
                            }
                        } else if (packetIn instanceof PacketPlayInTabComplete) {
                            PacketPlayInTabComplete request = (PacketPlayInTabComplete) packetIn;
                            String[] command = CustomStringUtils.splitStringToArgs(request.getText().substring(1));

                            List<TabCompleteMatches> matches = new ArrayList<>(Limbo.getInstance().getPluginManager().getTabOptions(player, command).stream().map(each -> new TabCompleteMatches(each)).collect(Collectors.toList()));

                            int start = CustomStringUtils.getIndexOfArg(request.getText(), command.length - 1) + 1;
                            int length = command[command.length - 1].length();

                            PacketPlayOutTabComplete response = new PacketPlayOutTabComplete(request.getId(), start, length, matches.toArray(new TabCompleteMatches[matches.size()]));
                            sendPacket(response);
                        } else if (packetIn instanceof PacketPlayInChat) {
                            PacketPlayInChat chat = (PacketPlayInChat) packetIn;
                            player.chat(chat.getMessage(), true, chat.getSignature(), chat.getTime());
                        } else if (packetIn instanceof ServerboundChatCommandPacket) {
                            ServerboundChatCommandPacket command = (ServerboundChatCommandPacket) packetIn;
                            Limbo.getInstance().dispatchCommand(player, "/" + command.getCommand());
                        } else if (packetIn instanceof PacketPlayInHeldItemChange) {
                            PacketPlayInHeldItemChange change = (PacketPlayInHeldItemChange) packetIn;
                            PlayerSelectedSlotChangeEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerSelectedSlotChangeEvent(player, (byte) change.getSlot()));
                            if (event.isCancelled()) {
                                PacketPlayOutHeldItemChange cancelPacket = new PacketPlayOutHeldItemChange(player.getSelectedSlot());
                                sendPacket(cancelPacket);
                            } else if (change.getSlot() != event.getSlot()) {
                                PacketPlayOutHeldItemChange changePacket = new PacketPlayOutHeldItemChange(event.getSlot());
                                sendPacket(changePacket);
                                Limbo.getInstance().getUnsafe().a(player, event.getSlot());
                            } else {
                                Limbo.getInstance().getUnsafe().a(player, event.getSlot());
                            }

                        } else if (packetIn instanceof ServerboundResourcePackPacket) {
                            ServerboundResourcePackPacket rpcheck = (ServerboundResourcePackPacket) packetIn;
                            // Pass on result to the events
                            Limbo.getInstance().getEventsManager().callEvent(new PlayerResourcePackStatusEvent(player, rpcheck.getAction()));
                            if (rpcheck.getAction().equals(Action.DECLINED) && properties.getResourcePackRequired()) {
                                player.disconnect(new TranslatableComponent("multiplayer.requiredTexturePrompt.disconnect"));
                            }
                        } else if (packetIn instanceof PacketPlayInPluginMessaging) {
                            PacketPlayInPluginMessaging inPluginMessaging = (PacketPlayInPluginMessaging) packetIn;
                            Limbo.getInstance().getEventsManager().callEvent(new PluginMessageEvent(player, inPluginMessaging.getChannel(), inPluginMessaging.getData()));
                        } else if (packetIn instanceof PacketPlayInBlockPlace) {
                            PacketPlayInBlockPlace packet = (PacketPlayInBlockPlace) packetIn;
                            Limbo.getInstance().getEventsManager().callEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, player.getEquipment().getItem(packet.getHand()), null, null, packet.getHand()));
                        } else if (packetIn instanceof PacketPlayInUseItem) {
                            PacketPlayInUseItem packet = (PacketPlayInUseItem) packetIn;
                            BlockState block = player.getWorld().getBlock(packet.getBlockHit().getBlockPos());
                            Limbo.getInstance().getEventsManager().callEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, player.getEquipment().getItem(packet.getHand()), block, packet.getBlockHit().getDirection(), packet.getHand()));
                        } else if (packetIn instanceof PacketPlayInSetCreativeSlot) {
                            PacketPlayInSetCreativeSlot packet = (PacketPlayInSetCreativeSlot) packetIn;
                            InventoryCreativeEvent event = Limbo.getInstance().getEventsManager().callEvent(new InventoryCreativeEvent(player.getInventoryView(), player.getInventory().getUnsafe().b().applyAsInt(packet.getSlotNumber()), packet.getItemStack()));
                            if (event.isCancelled()) {
                                player.updateInventory();
                            } else if (packet.getSlotNumber() == -1) {
                                // drop item / clone item
                                if (player.getGamemode() == GameMode.CREATIVE) {
                                    player.getInventory().setItem(player.getSelectedSlot(), event.getNewItem());
                                }
                                return;
                            } else {
                                player.getInventory().setItem(event.getSlot(), event.getNewItem());
                            }
                        } else if (packetIn instanceof PacketPlayInWindowClick) {
                            PacketPlayInWindowClick packet = (PacketPlayInWindowClick) packetIn;
                            try {
                                InventoryClickUtils.handle(player, packet);
                            } catch (Throwable e) {
                                e.printStackTrace();
                            }
                        } else if (packetIn instanceof PacketPlayInCloseWindow) {
                            PacketPlayInCloseWindow packet = (PacketPlayInCloseWindow) packetIn;
                            Inventory inventory = player.getInventoryView().getTopInventory();
                            if (inventory != null) {
                                Integer id = inventory.getUnsafe().c().get(player);
                                if (id != null) {
                                    Limbo.getInstance().getEventsManager().callEvent(new InventoryCloseEvent(player.getInventoryView()));
                                    player.getInventoryView().getUnsafe().a(null, null);
                                    inventory.getUnsafe().c().remove(player);
                                }
                            }
                        } else if (packetIn instanceof PacketPlayInBlockDig) {
                            PacketPlayInBlockDig packet = (PacketPlayInBlockDig) packetIn;
                            //noinspection SwitchStatementWithTooFewBranches
                            switch (packet.getAction()) {
                                case SWAP_ITEM_WITH_OFFHAND: {
                                    EntityEquipment equipment = player.getEquipment();
                                    PlayerSwapHandItemsEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerSwapHandItemsEvent(player, equipment.getItemInOffHand(), equipment.getItemInMainHand()));
                                    if (!event.isCancelled()) {
                                        equipment.setItemInMainHand(event.getMainHandItem());
                                        equipment.setItemInOffHand(event.getOffHandItem());
                                    }
                                    break;
                                }
                            }
                        } else if (packetIn instanceof PacketPlayInPickItem) {
                            PacketPlayInPickItem packet = (PacketPlayInPickItem) packetIn;
                            PlayerInventory inventory = player.getInventory();
                            int slot = inventory.getUnsafe().b().applyAsInt(packet.getSlot());
                            int i = player.getSelectedSlot();
                            byte selectedSlot = -1;
                            boolean firstRun = true;
                            while (selectedSlot < 0 || (!firstRun && i == player.getSelectedSlot())) {
                                ItemStack itemStack = inventory.getItem(i);
                                if (itemStack == null) {
                                    selectedSlot = (byte) i;
                                    break;
                                }
                                if (++i >= 9) {
                                    i = 0;
                                }
                            }
                            if (selectedSlot < 0) {
                                selectedSlot = player.getSelectedSlot();
                            }
                            ItemStack leavingHotbar = inventory.getItem(selectedSlot);
                            inventory.setItem(selectedSlot, inventory.getItem(slot));
                            inventory.setItem(slot, leavingHotbar);
                            player.setSelectedSlot(selectedSlot);
                        } else if (packetIn instanceof PacketPlayInItemName) {
                            PacketPlayInItemName packet = (PacketPlayInItemName) packetIn;
                            if (player.getInventoryView().getTopInventory() instanceof AnvilInventory) {
                                AnvilRenameInputEvent event = Limbo.getInstance().getEventsManager().callEvent(new AnvilRenameInputEvent(player.getInventoryView(), packet.getName()));
                                if (!event.isCancelled()) {
                                    AnvilInventory anvilInventory = (AnvilInventory) player.getInventoryView().getTopInventory();
                                    ItemStack result = anvilInventory.getItem(2);
                                    if (result != null) {
                                        result.displayName(LegacyComponentSerializer.legacySection().deserialize(event.getInput()));
                                    }
                                }
                            }
                        }
                    } catch (Exception ignored) {
                        break;
                    }
                }

                Limbo.getInstance().getEventsManager().callEvent(new PlayerQuitEvent(player));

                str = (properties.isLogPlayerIPAddresses() ? inetAddress.getHostName() : "<ip address withheld>") + ":" + session.getPort() + "|" + player.getName();
                Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player had disconnected!");
            }
        } catch (Exception ignored) {
        }

        try {
            channel.close();
            session.close();
        } catch (Exception ignored) {
        }
        state = ClientState.DISCONNECTED;

        if (player != null) {
            Limbo.getInstance().getUnsafe().b(player);
        }
        Limbo.getInstance().getServerConnection().getClients().remove(this);
        running = false;
    }

    public enum ClientState {
        LEGACY,
        HANDSHAKE,
        STATUS,
        LOGIN,
        CONFIGURATION,
        PLAY,
        DISCONNECTED;
    }

}
