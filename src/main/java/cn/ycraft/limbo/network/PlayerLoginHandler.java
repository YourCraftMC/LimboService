package cn.ycraft.limbo.network;

import cn.ycraft.limbo.config.ServerConfig;
import cn.ycraft.limbo.network.server.ForwardData;
import cn.ycraft.limbo.util.EntityUtil;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.player.PlayerJoinEvent;
import com.loohp.limbo.events.player.PlayerLoginEvent;
import com.loohp.limbo.events.player.PlayerSpawnEvent;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.player.PlayerInteractManager;
import com.loohp.limbo.utils.DeclareCommands;
import com.loohp.limbo.world.World;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import jdk.internal.foreign.abi.aarch64.AArch64Architecture;
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

public class PlayerLoginHandler implements ServerLoginHandler {
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

        if (!ServerConfig.ENFORCE_WHITELIST.getNotNull() && ServerConfig.whitelist.containsKey(profile.getId())) {
            session.disconnect(Component.text("You are not whitelisted on the server"));
            return;
        }

        Player player = new Player(clientConnection, profile.getName(), profile.getId(), Limbo.getInstance().getNextEntityId(), ServerConfig.WORLD_SPAWN.get(), new PlayerInteractManager());
        player.setSkinLayers((byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40));

        PlayerLoginEvent event = Limbo.getInstance().getEventsManager().callEvent(new PlayerLoginEvent(clientConnection, false, Component.empty()));
        if (event.isCancelled()) {
            session.disconnect(event.getCancelReason());
            return;
        }


        Limbo.getInstance().getUnsafe().a(player);

        Location worldSpawn = ServerConfig.WORLD_SPAWN.getNotNull();

        PlayerSpawnEvent spawnEvent = Limbo.getInstance().getEventsManager().callEvent(new PlayerSpawnEvent(player, worldSpawn));
        worldSpawn = spawnEvent.getSpawnLocation();
        World world = worldSpawn.getWorld();
        session.send(new ClientboundLoginPacket(0, false, new Key[]{Key.key("minecraft:" + world.getName())}, ServerConfig.MAX_PLAYERS.getNotNull(), 8, 8, !ServerConfig.REDUCED_DEBUG_INFO.getNotNull(), true, false, new PlayerSpawnInfo(world.getEnvironment().getId(), Key.key("minecraft:" + world.getName()), 0, ServerConfig.DEFAULT_GAMEMODE.getNotNull(), ServerConfig.DEFAULT_GAMEMODE.getNotNull(), false, true, null, 0, 0), false));
        Limbo.getInstance().getUnsafe().a(player, ServerConfig.DEFAULT_GAMEMODE.getNotNull());


        ByteBuf buffer = Unpooled.buffer();
        MinecraftTypes.writeString(buffer, Limbo.LIMBO_BRAND);
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        clientConnection.sendPluginMessage(ClientConnection.BRAND_ANNOUNCE_CHANNEL, bytes);

        ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(PlayerListEntryAction.ADD_PLAYER, PlayerListEntryAction.UPDATE_GAME_MODE, PlayerListEntryAction.UPDATE_LISTED, PlayerListEntryAction.UPDATE_LATENCY, PlayerListEntryAction.UPDATE_DISPLAY_NAME), new PlayerListEntry[]{new PlayerListEntry(profile.getId(), profile, true, 9, ServerConfig.DEFAULT_GAMEMODE.getNotNull(), Component.text(profile.getName()), true, 0, null, 0, null, null)});
        clientConnection.sendPacket(infoUpdatePacket);

        ClientboundPlayerAbilitiesPacket abilitiesPacket = new ClientboundPlayerAbilitiesPacket(true, ServerConfig.ALLOW_FLIGHT.getNotNull(), false, ServerConfig.DEFAULT_GAMEMODE.getNotNull() == GameMode.CREATIVE, 0.05F, 0.1F);
        clientConnection.sendPacket(abilitiesPacket);

        InetSocketAddress inetAddress = (InetSocketAddress) clientConnection.getInetAddress();
        String str = (ServerConfig.LOG_PLAYER_IP_ADDRESSES.getNotNull() ? inetAddress.getHostName() : "<ip address withheld>") + ":" + inetAddress.getPort() + "|" + player.getName() + "(" + player.getUniqueId() + ")";
        Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player had connected to the Limbo server!");

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

        // RESOURCEPACK CODE CONRIBUTED BY GAMERDUCK123
        if (!ServerConfig.RESOURCE_PACK.getNotNull().equalsIgnoreCase("")) {
            if (!ServerConfig.RESOURCE_PACK_SHA1.getNotNull().equalsIgnoreCase("")) {
                //SEND RESOURCEPACK
                player.setResourcePack(ServerConfig.RESOURCE_PACK.getNotNull(), ServerConfig.RESOURCE_PACK_SHA1.getNotNull(), ServerConfig.REQUIRED_RESOURCE_PACK.getNotNull(), GsonComponentSerializer.gson().deserialize(ServerConfig.RESOURCE_PACK_PROMPT.getNotNull()));
            } else {
                //NO SHA
                Limbo.getInstance().getConsole().sendMessage("ResourcePacks require SHA1s");
            }
        } else {
            //RESOURCEPACK NOT ENABLED
        }

        // PLAYER LIST HEADER AND FOOTER CODE CONRIBUTED BY GAMERDUCK123
        String headerStr = ServerConfig.TAB_HEADER.getNotNull();
        String footerStr = ServerConfig.TAB_FOOTER.getNotNull();
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
