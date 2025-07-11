package cn.ycraft.limbo.network;

import cn.ycraft.limbo.config.ServerConfig;
import cn.ycraft.limbo.event.connection.PacketReceiveEvent;
import cn.ycraft.limbo.util.ChunkUtil;
import cn.ycraft.limbo.util.ItemUtil;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.entity.EntityEquipment;
import com.loohp.limbo.events.inventory.AnvilRenameInputEvent;
import com.loohp.limbo.events.inventory.InventoryCloseEvent;
import com.loohp.limbo.events.inventory.InventoryCreativeEvent;
import com.loohp.limbo.events.player.*;
import com.loohp.limbo.inventory.AnvilInventory;
import com.loohp.limbo.inventory.EquipmentSlot;
import com.loohp.limbo.inventory.Inventory;
import com.loohp.limbo.inventory.ItemStack;
import com.loohp.limbo.location.BlockFace;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.player.PlayerInventory;
import com.loohp.limbo.registry.BuiltInRegistries;
import com.loohp.limbo.registry.RegistryCustom;
import com.loohp.limbo.utils.InventoryClickUtils;
import com.loohp.limbo.world.BlockState;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.querz.nbt.tag.CompoundTag;
import org.cloudburstmc.math.vector.Vector3i;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.DisconnectingEvent;
import org.geysermc.mcprotocollib.network.event.session.PacketSendingEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.data.game.RegistryEntry;
import org.geysermc.mcprotocollib.protocol.data.game.ResourcePackStatus;
import org.geysermc.mcprotocollib.protocol.data.game.entity.object.Direction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.Hand;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.common.serverbound.ServerboundResourcePackPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundFinishConfigurationPacket;
import org.geysermc.mcprotocollib.protocol.packet.configuration.clientbound.ClientboundRegistryDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandSuggestionsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundSetHeldSlotPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetChunkCacheCenterPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatCommandPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundCommandSuggestionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.inventory.*;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.player.*;

import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.BiConsumer;

public class ClientSessionPacketHandler extends SessionAdapter {
    BiConsumer<PlayerMoveEvent, Location> processMoveEvent = (event, originalTo) -> {
        if (event.isCancelled()) {
            Location returnTo = event.getFrom();
            ClientboundPlayerPositionPacket cancel = new ClientboundPlayerPositionPacket(1, returnTo.getX(), returnTo.getY(), returnTo.getZ(), 0, 0, 0, returnTo.getYaw(), returnTo.getPitch());
            event.getPlayer().clientConnection.sendPacket(cancel);
        } else {
            Location to = event.getTo();
            Limbo.getInstance().getUnsafe().a(event.getPlayer(), to);
            // If an event handler used setTo, let's make sure we tell the player about it.
            if (!originalTo.equals(to)) {
                ClientboundPlayerPositionPacket pos = new ClientboundPlayerPositionPacket(1, to.getX(), to.getY(), to.getZ(), 0, 0, 0, to.getYaw(), to.getPitch());
                event.getPlayer().clientConnection.sendPacket(pos);
            }
            ClientboundSetChunkCacheCenterPacket response = new ClientboundSetChunkCacheCenterPacket((int) event.getPlayer().getLocation().getX() >> 4, (int) event.getPlayer().getLocation().getZ() >> 4);
            event.getPlayer().clientConnection.sendPacket(response);
        }
    };

    private static final List<ClientboundRegistryDataPacket> REGISTRIES_DATA = new ArrayList<>();
    private static final ClientboundFinishConfigurationPacket FINISH_CONFIGURATION = new ClientboundFinishConfigurationPacket();

    static {
        try {
            for (RegistryCustom registryCustom : RegistryCustom.getRegistries()) {
                Map<Key, CompoundTag> map = registryCustom.getEntries();
                ArrayList<RegistryEntry> entries = new ArrayList<>();
                for (Map.Entry<Key, CompoundTag> entry : map.entrySet()) {
                    entries.add(new RegistryEntry(entry.getKey(), (NbtMap) ChunkUtil.convert(entry.getValue())));
                }
                ClientboundRegistryDataPacket registryDataPacket = new ClientboundRegistryDataPacket(registryCustom.getIdentifier(), entries);
                REGISTRIES_DATA.add(registryDataPacket);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void packetSending(PacketSendingEvent event) {
        Packet packet = event.getPacket();
        if (packet instanceof ClientboundRegistryDataPacket) {
            if (!REGISTRIES_DATA.contains(packet)) {
                event.setCancelled(true);
            }
        }
        if (packet instanceof ClientboundFinishConfigurationPacket) {
            if (packet != FINISH_CONFIGURATION) {
                event.setCancelled(true);
                for (ClientboundRegistryDataPacket registryDataPacket : REGISTRIES_DATA) {
                    event.getSession().send(registryDataPacket);
                }
                event.getSession().send(FINISH_CONFIGURATION);
            }
        }
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        Player player = session.getFlag(NetworkConstants.PLAYER_FLAG);
        if (player == null) {
            return;
        }
        if (Limbo.getInstance().getEventsManager().callEvent(new PacketReceiveEvent(player.clientConnection, packet)).isCancelled()) {
            return;
        }
        checkMovePacket(packet, player);
        checkCommandSuggestions(packet, player);
        checkChat(packet, player);
        checkCommand(packet, player);
        checkHeldSlot(packet, player);
        checkResourcePack(packet, player);
        checkPluginMessage(packet, player);
        checkInteractBlock(packet, player);
        checkInteractItem(packet, player);
        checkCreativeSlot(packet, player);
        checkContainerClick(packet, player);
        checkCloseContainer(packet, player);
        checkPlayerAction(packet, player);
        checkPickItemFromBlock(packet, player);
        checkRenameItem(packet, player);
    }

    @Override
    public void disconnecting(DisconnectingEvent event) {
        Session session = event.getSession();
        Player player = session.getFlag(NetworkConstants.PLAYER_FLAG);
        if (player == null) {
            return;
        }
        InetSocketAddress inetAddress = (InetSocketAddress) session.getRemoteAddress();

        Limbo.getInstance().getEventsManager().callEvent(new PlayerQuitEvent(player));
        Limbo.getInstance().getUnsafe().b(player);
        Limbo.getInstance().getServerConnection().getClients().remove(session);
        if (event.getCause() != null) {
            event.getCause().printStackTrace();
        }
    }

    private static void checkRenameItem(Packet packet, Player player) {
        if (packet instanceof ServerboundRenameItemPacket renameItemPacket) {
            if (player.getInventoryView().getTopInventory() instanceof AnvilInventory anvilInventory) {
                AnvilRenameInputEvent event = Limbo.getInstance().getEventsManager().callEvent(new AnvilRenameInputEvent(player.getInventoryView(), renameItemPacket.getName()));
                if (!event.isCancelled()) {
                    ItemStack result = anvilInventory.getItem(2);
                    if (result != null) {
                        result.displayName(LegacyComponentSerializer.legacySection().deserialize(event.getInput()));
                    }
                }
            }
        }
    }

    private static void checkPickItemFromBlock(Packet packet, Player player) {
        if (packet instanceof ServerboundPickItemFromBlockPacket pickItem) {
            PlayerInventory inventory = player.getInventory();
            Vector3i pos = pickItem.getPos();
            BlockState blockState = player.getWorld().getBlock(pos);
            int id = BuiltInRegistries.ITEM_REGISTRY.getId(blockState.getType());
            if (id > 0) {
                ItemStack itemStack = new ItemStack(blockState.getType(), 1);
                if (player.getGamemode() == GameMode.CREATIVE) {
                    inventory.setItem(player.getSelectedSlot(), itemStack);
                } else if (player.getGamemode() == GameMode.SURVIVAL) {
                    for (int i = 0; i < 9; i++) {
                        ItemStack item = inventory.getItem(i);
                        if (item != null && item.isSimilar(itemStack)) {
                            player.setSelectedSlot((byte) i);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void checkPlayerAction(Packet packet, Player player) {
        if (packet instanceof ServerboundPlayerActionPacket actionPacket) {
            //noinspection SwitchStatementWithTooFewBranches
            switch (actionPacket.getAction()) {
                case SWAP_HANDS: {
                    EntityEquipment equipment = player.getEquipment();
                    PlayerSwapHandItemsEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerSwapHandItemsEvent(player, equipment.getItemInOffHand(), equipment.getItemInMainHand()));
                    if (!event.isCancelled()) {
                        equipment.setItemInMainHand(event.getMainHandItem());
                        equipment.setItemInOffHand(event.getOffHandItem());
                    }
                    break;
                }
            }
        }
    }

    private static void checkCloseContainer(Packet packet, Player player) {
        if (packet instanceof ServerboundContainerClosePacket close) {
            Inventory inventory = player.getInventoryView().getTopInventory();
            if (inventory != null) {
                Integer id = inventory.getUnsafe().c().get(player);
                if (id != null) {
                    Limbo.getInstance().getEventsManager().callEvent(new InventoryCloseEvent(player.getInventoryView()));
                    player.getInventoryView().getUnsafe().a(null, null);
                    inventory.getUnsafe().c().remove(player);
                }
            }
        }
    }

    private static void checkContainerClick(Packet packet, Player player) {
        if (packet instanceof ServerboundContainerClickPacket clickPacket) {
            try {
                InventoryClickUtils.handle(player, clickPacket);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    private static void checkCreativeSlot(Packet packet, Player player) {
        if (packet instanceof ServerboundSetCreativeModeSlotPacket creativeSlot) {
            InventoryCreativeEvent event = Limbo.getInstance().getEventsManager().callEvent(new InventoryCreativeEvent(player.getInventoryView(), player.getInventory().getUnsafe().b().applyAsInt(creativeSlot.getSlot()), ItemUtil.to(creativeSlot.getClickedItem())));
            if (event.isCancelled()) {
                player.updateInventory();
            } else if (creativeSlot.getSlot() == -1) {
                // drop item / clone item
                if (player.getGamemode() == GameMode.CREATIVE) {
                    player.getInventory().setItem(player.getSelectedSlot(), event.getNewItem());
                }
            } else {
                player.getInventory().setItem(event.getSlot(), event.getNewItem());
            }
        }
    }

    private static void checkInteractBlock(Packet packet, Player player) {
        if (packet instanceof ServerboundUseItemOnPacket useItem) {
            BlockState block = player.getWorld().getBlock(useItem.getPosition());
            Hand hand = useItem.getHand();
            EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            Direction face = useItem.getFace();
            BlockFace blockFace = null;
            switch (face) {
                case UP:
                    blockFace = BlockFace.UP;
                    break;
                case DOWN:
                    blockFace = BlockFace.DOWN;
                    break;
                case NORTH:
                    blockFace = BlockFace.NORTH;
                    break;
                case SOUTH:
                    blockFace = BlockFace.SOUTH;
                    break;
                case WEST:
                    blockFace = BlockFace.WEST;
                    break;
                case EAST:
                    blockFace = BlockFace.EAST;
                    break;
            }
            Limbo.getInstance().getEventsManager().callEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, player.getEquipment().getItem(slot), block, blockFace, slot));
        }
    }

    private static void checkInteractItem(Packet packet, Player player) {
        if (packet instanceof ServerboundUseItemPacket itemPacket) {
            Hand hand = itemPacket.getHand();
            EquipmentSlot slot = hand == Hand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
            Limbo.getInstance().getEventsManager().callEvent(new PlayerInteractEvent(player, PlayerInteractEvent.Action.RIGHT_CLICK_AIR, player.getEquipment().getItem(slot), null, null, slot));
        }
    }

    private static void checkPluginMessage(Packet packet, Player player) {
        if (packet instanceof ServerboundCustomPayloadPacket inPluginMessaging) {
            Limbo.getInstance().getEventsManager().callEvent(new PluginMessageEvent(player, inPluginMessaging.getChannel().asString(), inPluginMessaging.getData()));
        }
    }

    private static void checkResourcePack(Packet packet, Player player) {
        if (packet instanceof ServerboundResourcePackPacket rpcheck) {
            // Pass on result to the events
            Limbo.getInstance().getEventsManager().callEvent(new PlayerResourcePackStatusEvent(player, rpcheck.getStatus()));
            if (rpcheck.getStatus() == ResourcePackStatus.DECLINED && ServerConfig.RESOURCE_PACK.FORCE.resolve()) {
                player.disconnect(Component.translatable("multiplayer.requiredTexturePrompt.disconnect"));
            }
        }
    }

    private static void checkHeldSlot(Packet packet, Player player) {
        if (packet instanceof ServerboundSetCarriedItemPacket change) {
            PlayerSelectedSlotChangeEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerSelectedSlotChangeEvent(player, (byte) change.getSlot()));
            if (event.isCancelled()) {
                ClientboundSetHeldSlotPacket cancelPacket = new ClientboundSetHeldSlotPacket(player.getSelectedSlot());
                player.clientConnection.sendPacket(cancelPacket);
            } else if (change.getSlot() != event.getSlot()) {
                ClientboundSetHeldSlotPacket changePacket = new ClientboundSetHeldSlotPacket(event.getSlot());
                player.clientConnection.sendPacket(changePacket);
                Limbo.getInstance().getUnsafe().a(player, event.getSlot());
            } else {
                Limbo.getInstance().getUnsafe().a(player, event.getSlot());
            }

        }
    }

    private static void checkCommand(Packet packet, Player player) {
        if (packet instanceof ServerboundChatCommandPacket command) {
            Limbo.getInstance().dispatchCommand(player, "/" + command.getCommand());
        }
    }

    private static void checkChat(Packet packet, Player player) {
        if (packet instanceof ServerboundChatPacket chat) {
            player.chat(chat.getMessage(), true, chat.getSignature(), Instant.ofEpochMilli(chat.getTimeStamp()));
        }
    }

    private void checkCommandSuggestions(Packet packet, Player player) {
        if (packet instanceof ServerboundCommandSuggestionPacket request) {
            StringReader input = new StringReader(request.getText());
            if (input.canRead() && input.peek() == '/') {
                input.skip();
            }
            CompletableFuture<Suggestions> future = Limbo.getInstance().getPluginManager().suggest(player, input);

            future.thenAccept(suggestions -> {
                int start = suggestions.getRange().getStart();
                int length = suggestions.getRange().getLength();

                String[] texts = suggestions.getList().stream().map(Suggestion::getText).toArray(String[]::new);
                Component[] tooltips = suggestions.getList().stream().map(s -> Component.text(s.getTooltip().getString())).toArray(Component[]::new);
                ClientboundCommandSuggestionsPacket response = new ClientboundCommandSuggestionsPacket(request.getTransactionId(), start, length, texts, tooltips);
                player.clientConnection.sendPacket(response);
            });
        }
    }

    private void checkMovePacket(Packet packet, Player player) {
        if (packet instanceof ServerboundMovePlayerPosRotPacket pos) {//move_player_pos_rot
            Location from = player.getLocation();
            Location to = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), pos.getYaw(), pos.getPitch());

            if (!from.equals(to)) {
                PlayerMoveEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerMoveEvent(player, from, to));
                processMoveEvent.accept(event, to);
            }
        } else if (packet instanceof ServerboundMovePlayerPosPacket pos) {//move_player_pos
            Location from = player.getLocation();
            Location to = new Location(player.getWorld(), pos.getX(), pos.getY(), pos.getZ(), player.getLocation().getYaw(), player.getLocation().getPitch());

            if (!from.equals(to)) {
                PlayerMoveEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerMoveEvent(player, from, to));
                processMoveEvent.accept(event, to);
            }
        } else if (packet instanceof ServerboundMovePlayerRotPacket pos) {//move_player_rot
            Location from = player.getLocation();
            Location to = new Location(player.getWorld(), player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ(), pos.getYaw(), pos.getPitch());

            if (!from.equals(to)) {
                PlayerMoveEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerMoveEvent(player, from, to));
                processMoveEvent.accept(event, to);
            }
        }
    }
}
