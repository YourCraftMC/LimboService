package com.loohp.limbo;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.server.ServerAdapter;
import org.geysermc.mcprotocollib.network.event.server.ServerClosedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionAddedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionRemovedEvent;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.network.server.NetworkServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.codec.MinecraftCodec;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.PlayerSpawnInfo;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundLoginPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundSystemChatPacket;
import org.geysermc.mcprotocollib.protocol.packet.ingame.serverbound.ServerboundChatPacket;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;

public class Server {
    private NetworkServer server;

    public Server(SocketAddress address) {
        LoggerFactory.getLogger("Server").info("a");
        this.server = new NetworkServer(address, MinecraftProtocol::new);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, false);
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, session ->
                new ServerStatusInfo(
                        Component.text("Limbo"),
                        new PlayerInfo(-1, 1, new ArrayList<>()),
                        new VersionInfo(MinecraftCodec.CODEC.getMinecraftVersion(), MinecraftCodec.CODEC.getProtocolVersion()),
                        null, false
                ));
        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, session ->
                session.send(new ClientboundLoginPacket(
                        0,
                        false,
                        new Key[]{Key.key("minecraft:world")},
                        0,
                        16,
                        16,
                        false,
                        false,
                        false,
                        new PlayerSpawnInfo(
                                0,
                                Key.key("minecraft:world"),
                                100,
                                GameMode.SURVIVAL,
                                GameMode.SURVIVAL,
                                false,
                                false,
                                null,
                                100,
                                5
                        ),
                        false
                ))
        );
        server.addListener(new ServerAdapter() {
            @Override
            public void serverClosed(ServerClosedEvent event) {
                System.out.println("Server closed.");
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
                event.getSession().addListener(new SessionAdapter() {
                    @Override
                    public void packetReceived(Session session, Packet packet) {
                        if (packet instanceof ServerboundChatPacket) {
                            ServerboundChatPacket chatPacket = (ServerboundChatPacket) packet;
                            GameProfile profile = event.getSession().getFlag(MinecraftConstants.PROFILE_KEY);
                            System.out.println(profile.getName() + ": " + chatPacket.getMessage());

                            Component msg = Component.text("Hello, ")
                                    .color(NamedTextColor.GREEN)
                                    .append(Component.text(profile.getName())
                                            .color(NamedTextColor.AQUA)
                                            .decorate(TextDecoration.UNDERLINED))
                                    .append(Component.text("!")
                                            .color(NamedTextColor.GREEN));

                            session.send(new ClientboundSystemChatPacket(msg, false));
                        }
                    }
                });
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                MinecraftProtocol protocol = (MinecraftProtocol) event.getSession().getPacketProtocol();
                if (protocol.getOutboundState() == ProtocolState.GAME) {
                    System.out.println("Closing server.");
                    event.getServer().close(false);
                }
            }
        });

        server.bind();
    }

    public static void main(String[] args) {
        new Server(new InetSocketAddress(25565));
    }

}
