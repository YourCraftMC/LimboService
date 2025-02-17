package com.loohp.limbo.network;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.player.PlayerJoinEvent;
import com.loohp.limbo.events.player.PlayerSpawnEvent;
import com.loohp.limbo.file.ServerProperties;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.player.PlayerInteractManager;
import com.loohp.limbo.utils.DeclareCommands;
import com.loohp.limbo.world.World;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import org.cloudburstmc.math.vector.Vector3d;
import org.cloudburstmc.math.vector.Vector3i;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Flag;
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
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerAbilitiesPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.entity.player.ClientboundPlayerPositionPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundGameEventPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.level.ClientboundSetDefaultSpawnPositionPacket;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.EnumSet;

public class PlayerLoginHandler implements ServerLoginHandler {
    public static final Flag<Player> PLAYER_FLAG = new Flag<>("limbo:player", Player.class);
    private ClientConnection clientConnection;

    public PlayerLoginHandler(ClientConnection clientConnection) {
        this.clientConnection = clientConnection;
    }

    @Override
    public void loggedIn(Session session) {

        GameProfile profile = session.getFlag(MinecraftConstants.PROFILE_KEY);
        Player player = new Player(clientConnection, profile.getName(), profile.getId(), Limbo.getInstance().getNextEntityId(), Limbo.getInstance().getServerProperties().getWorldSpawn(), new PlayerInteractManager());
        player.setSkinLayers((byte) (0x01 | 0x02 | 0x04 | 0x08 | 0x10 | 0x20 | 0x40));

        ServerProperties properties = Limbo.getInstance().getServerProperties();
        Location worldSpawn = properties.getWorldSpawn();

        PlayerSpawnEvent spawnEvent = Limbo.getInstance().getEventsManager().callEvent(new PlayerSpawnEvent(player, worldSpawn));
        worldSpawn = spawnEvent.getSpawnLocation();
        World world = worldSpawn.getWorld();
        session.send(new ClientboundLoginPacket(
                0,
                false,
                new Key[]{Key.key("minecraft:" + world.getName())},
                20,
                16,
                16,
                false,
                false,
                false,
                new PlayerSpawnInfo(
                        0,//todo
                        Key.key("minecraft:" + world.getName()),
                        100,
                        properties.getDefaultGamemode(),
                        properties.getDefaultGamemode(),
                        !properties.isReducedDebugInfo(),
                        false,
                        null,
                        100,
                        5
                ),
                false
        ));
        Limbo.getInstance().getUnsafe().a(player, properties.getDefaultGamemode());


        ByteBuf buffer = Unpooled.buffer();
        MinecraftTypes.writeString(buffer, properties.getServerModName());
        byte[] bytes = new byte[buffer.readableBytes()];
        buffer.readBytes(bytes);
        clientConnection.sendPluginMessage(ClientConnection.BRAND_ANNOUNCE_CHANNEL, bytes);

        ClientboundPlayerInfoUpdatePacket infoUpdatePacket = new ClientboundPlayerInfoUpdatePacket(EnumSet.of(PlayerListEntryAction.ADD_PLAYER), new PlayerListEntry[]{new PlayerListEntry(
                profile.getId(),
                profile,
                true,
                9,
                properties.getDefaultGamemode(),
                Component.text(profile.getName()),
                true,
                0,
                null,
                0,
                null,
                null
        )});
        clientConnection.sendPacket(infoUpdatePacket);

        ClientboundPlayerAbilitiesPacket abilitiesPacket = new ClientboundPlayerAbilitiesPacket(true, properties.isAllowFlight(), false, properties.getDefaultGamemode() == GameMode.CREATIVE, 0.05F, 0.1F);
        clientConnection.sendPacket(abilitiesPacket);

        InetSocketAddress inetAddress = (InetSocketAddress) clientConnection.getInetAddress();
        String str = (properties.isLogPlayerIPAddresses() ? inetAddress.getHostName() : "<ip address withheld>") + ":" + inetAddress.getPort() + "|" + player.getName() + "(" + player.getUniqueId() + ")";
        Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player had connected to the Limbo server!");

        ClientboundGameEventPacket chunkStartEvent = new ClientboundGameEventPacket(GameEvent.LEVEL_CHUNKS_LOAD_START, null);
        clientConnection.sendPacket(chunkStartEvent);

        player.playerInteractManager.update();

        ClientboundCommandsPacket commandsPacket = DeclareCommands.getDeclareCommandsPacket(player);
        player.clientConnection.sendPacket(commandsPacket);

        ClientboundSetDefaultSpawnPositionPacket spawnPositionPacket = new ClientboundSetDefaultSpawnPositionPacket(Vector3i.ZERO, worldSpawn.getPitch());
        player.clientConnection.sendPacket(spawnPositionPacket);

        Vector3d spawn = Vector3d.from(worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ());
        ClientboundPlayerPositionPacket positionPacket = new ClientboundPlayerPositionPacket(player.getEntityId(), spawn, Vector3d.ZERO, player.getYaw(), player.getPitch(), new ArrayList<>());
        Limbo.getInstance().getUnsafe().a(player, new Location(world, worldSpawn.getX(), worldSpawn.getY(), worldSpawn.getZ(), worldSpawn.getYaw(), worldSpawn.getPitch()));
        player.clientConnection.sendPacket(positionPacket);

        try {
            player.getDataWatcher().update();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        //todo metadata
        Limbo.getInstance().getEventsManager().callEvent(new PlayerJoinEvent(player));

        if (properties.isAllowFlight()) {
            ClientboundGameEventPacket state = new ClientboundGameEventPacket(GameEvent.CHANGE_GAMEMODE, properties.getDefaultGamemode());
            player.clientConnection.sendPacket(state);
        }

        // PLAYER LIST HEADER AND FOOTER CODE CONRIBUTED BY GAMERDUCK123
        player.sendPlayerListHeaderAndFooter(properties.getTabHeader(), properties.getTabFooter());

        session.setFlag(PLAYER_FLAG, player);
    }
}
