package com.loohp.limbo.network;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.player.PlayerSpawnEvent;
import com.loohp.limbo.file.ServerProperties;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.player.PlayerInteractManager;
import com.loohp.limbo.world.World;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.ServerLoginHandler;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;

public class PlayerLoginHandler implements ServerLoginHandler {
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
                new Key[]{Key.key("minecraft:"+world.getName())},
                20,
                16,
                16,
                false,
                false,
                false,
                new PlayerSpawnInfo(
                        0,//todo
                        Key.key("minecraft:"+world.getName()),
                        100,
                        player.getGamemode(),
                        player.getGamemode(),
                        !properties.isReducedDebugInfo(),
                        false,
                        null,
                        100,
                        5
                ),
                false
        ));
    }
}
