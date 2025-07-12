package cn.ycraft.limbo.network;

import cn.ycraft.limbo.config.AllowlistConfig;
import cn.ycraft.limbo.config.ServerConfig;
import cn.ycraft.limbo.config.ServerMessages;
import cn.ycraft.limbo.network.server.ForwardData;
import cn.ycraft.limbo.util.EntityUtil;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.player.PlayerJoinEvent;
import com.loohp.limbo.events.player.PlayerLoginEvent;
import com.loohp.limbo.events.player.PlayerSpawnEvent;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.player.PlayerInteractManager;
import com.loohp.limbo.utils.DeclareCommands;
import com.loohp.limbo.utils.MojangAPIUtils;
import com.loohp.limbo.world.World;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.ServerLoginHandler;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftTypes;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntry;
import org.geysermc.mcprotocollib.protocol.data.game.PlayerListEntryAction;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.game.level.notify.GameEvent;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundPlayerInfoUpdatePacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.ClientboundSetEntityDataPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerAbilitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerLoginHandler implements ServerLoginHandler {
    private static Cache<UUID, List<GameProfile.Property>> propertiesCache = CacheBuilder.newBuilder().expireAfterAccess(30, TimeUnit.SECONDS).build();

    @Override
    public void loggedIn(Session session) {
        ClientConnection clientConnection = session.getFlag(NetworkConstants.CLIENT_CONNECTION_FLAG);
        ClientSessionPacketHandler packetHandler = session.getFlag(NetworkConstants.CLIENT_SESSION_PACKET_HANDLER_FLAG);
        ForwardData forwardedData = session.getFlag(NetworkConstants.FORWARD_FLAG);
        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);

        if (forwardedData.uuid != null) {
            profile = new GameProfile(forwardedData.uuid, forwardedData.username != null ? forwardedData.username : profile.getName());
        }
        if (forwardedData.velocityDataFrom != null) {
            profile = new GameProfile(forwardedData.velocityDataFrom.uuid, forwardedData.velocityDataFrom.username != null ? forwardedData.velocityDataFrom.username : profile.getName());
        }
        if (forwardedData.properties != null) {
            profile.setProperties(forwardedData.properties);
        }
        if (forwardedData.velocityDataFrom != null) {
            profile.setProperties(forwardedData.velocityDataFrom.properties);
        }
        if (ServerConfig.PLAYER.OVERWRITE_SKIN.resolve() && Limbo.getInstance().isOnlineMode()) {
            List<GameProfile.Property> properties = propertiesCache.getIfPresent(profile.getId());
            if (properties == null) {
                properties = MojangAPIUtils.getPropertiesFromMojangServer(profile.getId());
            }
            if (properties != null) {
                propertiesCache.put(profile.getId(), properties);
                profile.setProperties(properties);
            }
        }

        if (!AllowlistConfig.isAllowed(profile.getId(), profile.getName())) {
            session.disconnect(ServerMessages.NOT_ALLOWED.compileLine(null));
            return;
        }

        Player player = new Player(clientConnection, profile.getName(), profile.getId(), Limbo.getInstance().getNextEntityId(), ServerConfig.WORLD.SPAWNPOINT.resolve(), new PlayerInteractManager());
        player.setSkinLayers((byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40));

        PlayerLoginEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerLoginEvent(clientConnection, false, Component.empty()));
        if (event.isCancelled()) {
            session.disconnect(event.getCancelReason());
            return;
        }


        Limbo.getInstance().getUnsafe().a(player);

        Location worldSpawn = ServerConfig.WORLD.SPAWNPOINT.resolve();

        PlayerSpawnEvent spawnEvent = Limbo.getInstance().getEventsManager().callEvent(new PlayerSpawnEvent(player, worldSpawn));
        worldSpawn = spawnEvent.getSpawnLocation();
        World world = worldSpawn.getWorld();
        session.send(new ClientboundLoginPacket(0, false, new Key[]{Key.key("minecraft:" + world.getName())}, ServerConfig.SERVER.MAX_PLAYERS.resolve(), 8, 8, !ServerConfig.LOGS.REDUCED_DEBUG_INFO.resolve(), true, false, new PlayerSpawnInfo(world.getEnvironment().getId(), Key.key("minecraft:" + world.getName()), 0, ServerConfig.PLAYER.DEFAULT_GAMEMODE.resolve(), ServerConfig.PLAYER.DEFAULT_GAMEMODE.resolve(), false, true, null, 0, 0), false));
        Limbo.getInstance().getUnsafe().a(player, ServerConfig.PLAYER.DEFAULT_GAMEMODE.resolve());

        ByteBuf buffer = Unpooled.buffer();
        MinecraftTypes.writeString(buffer, Limbo.LIMBO_BRAND);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        clientConnection.sendPluginMessage(ClientConnection.BRAND_ANNOUNCE_CHANNEL, bytes);

        ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LISTED, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_DISPLAY_NAME), new PlayerListEntry[]{new PlayerListEntry(profile.getId(), profile, true, 9, ServerConfig.PLAYER.DEFAULT_GAMEMODE.resolve(), Component.text(profile.getName()), true, 0, null, 0, null, null)});
        clientConnection.sendPacket(infoUpdatePacket);

        ClientboundPlayerAbilitiesPacket abilitiesPacket = new ClientboundPlayerAbilitiesPacket(true, ServerConfig.PLAYER.ALLOW_FLIGHT.resolve(), false, ServerConfig.PLAYER.DEFAULT_GAMEMODE.resolve() == GameMode.CREATIVE, 0.05F, 0.1F);
        clientConnection.sendPacket(abilitiesPacket);

        if (ServerConfig.LOGS.CONNECTION_VERBOSE.resolve()) {
            InetSocketAddress inetAddress = (InetSocketAddress) clientConnection.getInetAddress();
            String str = (ServerConfig.LOGS.DISPLAY_IP_ADDRESS.resolve() ? inetAddress.getHostName() : "") + ":" + inetAddress.getPort() + "|" + player.getName() + "(" + player.getUniqueId() + ")";
            Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player had connected to the Limbo server!");
        }

        ClientboundGameEventPacket chunkStartEvent = new ClientboundGameEventPacket(GameEvent.LEVEL_CHUNKS_LOAD_START, null);
        clientConnection.sendPacket(chunkStartEvent);

        player.playerInteractManager.update();

        ClientboundCommandsPacket commandsPacket = DeclareCommands.getDeclareCommandsPacket(player);
        player.clientConnection.sendPacket(commandsPacket);

        ClientboundSetDefaultSpawnPositionPacket spawnPositionPacket = new ClientboundSetDefaultSpawnPositionPacket(Vector3i.ZERO, worldSpawn.getPitch());
        player.clientConnection.sendPacket(spawnPositionPacket);

        Vector3d spawn = Vector3d.from(worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ());
        ClientboundPlayerPositionPacket positionPacket = new ClientboundPlayerPositionPacket(1, spawn, Vector3d.ZERO, player.getYaw(), player.getPitch(), new ArrayList<>());
        Limbo.getInstance().getUnsafe().a(player, new Location(world, worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ(), worldSpawn.getYaw(), worldSpawn.getPitch()));
        player.clientConnection.sendPacket(positionPacket);

        try {
            player.getDataWatcher().update();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        try {
            ClientboundSetEntityDataPacket metadata = EntityUtil.metadata(player, false, Player.class.getDeclaredField("skinLayers"));
            player.clientConnection.sendPacket(metadata);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        Limbo.getInstance().getEventsManager().callEvent(new PlayerJoinEvent(player));

//        if (properties.isAllowFlight()) {
//            ClientboundGameEventPacket state = new ClientboundGameEventPacket(GameEvent.CHANGE_GAMEMODE, properties.getDefaultGamemode());
//            player.clientConnection.sendPacket(state);
//        }

        // RESOURCEPACK CODE CONTRIBUTED BY GAMERDUCK123
        if (!ServerConfig.RESOURCE_PACK.URL.resolve().isEmpty()) {
            if (!ServerConfig.RESOURCE_PACK.SHA1.resolve().equalsIgnoreCase("")) {
                //SEND RESOURCEPACK
                player.setResourcePack(ServerConfig.RESOURCE_PACK.URL.resolve(), ServerConfig.RESOURCE_PACK.SHA1.resolve(), ServerConfig.RESOURCE_PACK.FORCE.resolve(), GsonComponentSerializer.gson().deserialize(ServerConfig.RESOURCE_PACK.PROMPT.resolve()));
            } else {
                //NO SHA
                Limbo.getInstance().getConsole().sendMessage("ResourcePacks require SHA1s");
            }
        } else {
            //RESOURCEPACK NOT ENABLED
        }

        // PLAYER LIST HEADER AND FOOTER CODE CONTRIBUTED BY GAMERDUCK123
        String headerStr = ServerConfig.SERVER.TAB_LIST.HEADER.resolve();
        String footerStr = ServerConfig.SERVER.TAB_LIST.FOOTER.resolve();
        Component header = Component.empty();
        Component footer = Component.empty();
        if (!headerStr.isEmpty()) {
            header = GsonComponentSerializer.gson().deserialize(headerStr);
        }
        if (!footerStr.isEmpty()) {
            footer = GsonComponentSerializer.gson().deserialize(footerStr);
        }
        player.sendPlayerListHeaderAndFooter(header, footer);

        session.setFlag(NetworkConstants.PLAYER_FLAG, player);
    }
}
