/*
  ~ This file is part of Limbo.
  ~
  ~ Copyright (C) 2024. YourCraftMC <admin@ycraft.cn>
  ~ Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
  ~ Copyright (C) 2022. Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
 */

package cn.ycraft.limbo.network;

import cn.ycraft.limbo.config.ServerConfig;
import cn.ycraft.limbo.network.protocol.LimboProtocol;
import cn.ycraft.limbo.network.server.ForwardData;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.connection.ConnectionEstablishedEvent;
import com.loohp.limbo.events.player.PlayerQuitEvent;
import com.loohp.limbo.events.status.StatusPingEvent;
import com.loohp.limbo.player.Player;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.server.ServerAdapter;
import org.geysermc.mcprotocollib.network.event.server.ServerBoundEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionAddedEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionRemovedEvent;
import org.geysermc.mcprotocollib.network.server.NetworkServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerConnection {

    private final String ip;
    private final int port;
    private final boolean onlineMode;
    private final boolean silent;
    private final Map<Session, ClientConnection> clients;

    private NetworkServer server;

    public ServerConnection(String ip, int port, boolean onlineMode, boolean silent) {
        this.clients = new HashMap<>();
        this.ip = ip;
        this.port = port;
        this.onlineMode = onlineMode;
        this.silent = silent;
        start();
    }

    public NetworkServer getServer() {
        return server;
    }

    void start() {
        server = new NetworkServer(new InetSocketAddress(this.ip, this.port), LimboProtocol::new);
        server.setGlobalFlag(MinecraftConstants.ENCRYPT_CONNECTION, onlineMode);
        server.setGlobalFlag(MinecraftConstants.SHOULD_AUTHENTICATE, onlineMode);
        server.setGlobalFlag(MinecraftConstants.SERVER_LOGIN_HANDLER_KEY, new PlayerLoginHandler());
        clients();
        motd();
        server.bind();
    }

    private void clients() {
        server.addListener(new ServerAdapter() {
            @Override
            public void serverBound(ServerBoundEvent event) {
                if (!silent) {
                    Limbo.getInstance().getConsole().sendMessage("LimboService start listening on /" + ((InetSocketAddress) event.getServer().getBindAddress()).getHostName() + ":" + ((InetSocketAddress) event.getServer().getBindAddress()).getPort());
                }
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
                ClientSessionPacketHandler packetHandler = new ClientSessionPacketHandler();
                ClientConnection sc = new ClientConnection(event.getSession());
                event.getSession().setFlag(NetworkConstants.CLIENT_CONNECTION_FLAG, sc);
                event.getSession().setFlag(NetworkConstants.CLIENT_SESSION_PACKET_HANDLER_FLAG, packetHandler);
                event.getSession().setFlag(NetworkConstants.FORWARD_FLAG, new ForwardData());
                event.getSession().addListener(packetHandler);
                event.getSession().addListener(sc);
                clients.put(event.getSession(), sc);
                Limbo.getInstance().getEventsManager().callEvent(new ConnectionEstablishedEvent(sc));

                if (ServerConfig.LOGS.HANDSHAKE_VERBOSE.resolve()) {
                    InetSocketAddress inetAddress = ((InetSocketAddress) event.getSession().getRemoteAddress());
                    String str = (ServerConfig.LOGS.DISPLAY_IP_ADDRESS.resolve() ? inetAddress.getHostName() : "") + ":" + inetAddress.getPort();
                    Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Legacy Status has pinged");
                }
            }

            @Override
            public void sessionRemoved(SessionRemovedEvent event) {
                super.sessionRemoved(event);
                Session session = event.getSession();
                Player player = session.getFlag(NetworkConstants.PLAYER_FLAG);
                if (player == null) {
                    return;
                }
                InetSocketAddress inetAddress = (InetSocketAddress) session.getRemoteAddress();

                Limbo.getInstance().getEventsManager().callEvent(new PlayerQuitEvent(player));

                if (ServerConfig.LOGS.CONNECTION_VERBOSE.resolve()) {
                    String str = (ServerConfig.LOGS.DISPLAY_IP_ADDRESS.resolve() ? inetAddress.getHostName() : "") + ":" + inetAddress.getPort() + "|" + player.getName();
                    Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player had disconnected!");
                }
                Limbo.getInstance().getUnsafe().b(player);
                Limbo.getInstance().getServerConnection().getClients().remove(session);
            }
        });
    }

    private void motd() {
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, session -> {
            StatusPingEvent event = Limbo.getInstance().getEventsManager().callEvent(
                new StatusPingEvent(
                    getClient(session),
                    ServerConfig.SERVER.VERSION.resolve(),
                    Limbo.SERVER_IMPLEMENTATION_PROTOCOL,
                    GsonComponentSerializer.gson().deserialize(ServerConfig.SERVER.MOTD.resolve()),
                    ServerConfig.SERVER.MAX_PLAYERS.resolve(),
                    Limbo.getInstance().getPlayers().size(),
                    ServerConfig.SERVER.FAVICON.resolve().data()
                )
            );
            return new ServerStatusInfo(event.getMotd(),
                new PlayerInfo(event.getMaxPlayers(), event.getPlayersOnline(), new ArrayList<>()),
                new VersionInfo(event.getVersion(), event.getProtocol()),
                event.getFavicon(), false
            );
        });
    }

    public Map<Session, ClientConnection> getClients() {
        return clients;
    }

    public ClientConnection getClient(Session session) {
        return clients.get(session);
    }

    public void shutdown() {
        server.close(false);
    }
}
