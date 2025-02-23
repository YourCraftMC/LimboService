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
import com.loohp.limbo.events.status.StatusPingEvent;
import com.loohp.limbo.file.ServerProperties;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.server.ServerAdapter;
import org.geysermc.mcprotocollib.network.event.server.ServerBoundEvent;
import org.geysermc.mcprotocollib.network.event.server.SessionAddedEvent;
import org.geysermc.mcprotocollib.network.server.NetworkServer;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.data.status.PlayerInfo;
import org.geysermc.mcprotocollib.protocol.data.status.ServerStatusInfo;
import org.geysermc.mcprotocollib.protocol.data.status.VersionInfo;
import org.geysermc.mcprotocollib.protocol.data.status.handler.ServerInfoBuilder;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ServerConnection {

    private final String ip;
    private final int port;
    private final boolean silent;
    private ServerSocket serverSocket;
    private Map<Session, ClientConnection> clients;

    private NetworkServer server;

    public ServerConnection(String ip, int port, boolean silent) {
        this.clients = new HashMap<>();
        this.ip = ip;
        this.port = port;
        this.silent = silent;
        start();
    }

    void start() {
        server = new NetworkServer(new InetSocketAddress(this.ip, this.port), MinecraftProtocol::new);
        clients();
        motd();
        server.bind();
    }

    private void clients() {
        server.addListener(new ServerAdapter() {
            @Override
            public void serverBound(ServerBoundEvent event) {
                if (!silent) {
                    Limbo.getInstance().getConsole().sendMessage("Limbo server listening on /" + serverSocket.getInetAddress().getHostName() + ":" + serverSocket.getLocalPort());
                }
            }

            @Override
            public void sessionAdded(SessionAddedEvent event) {
                ClientConnection sc = new ClientConnection(event.getSession());
                clients.put(event.getSession(), sc);

                InetSocketAddress inetAddress = ((InetSocketAddress) event.getSession().getRemoteAddress());
                ServerProperties properties = Limbo.getInstance().getServerProperties();
                String str = (properties.isLogPlayerIPAddresses() ? inetAddress.getHostName() : "<ip address withheld>") + ":" + inetAddress.getPort();
                Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Legacy Status has pinged");
            }
        });
    }

    private void motd() {
        server.setGlobalFlag(MinecraftConstants.SERVER_INFO_BUILDER_KEY, new ServerInfoBuilder() {
            @Override
            public ServerStatusInfo buildInfo(Session session) {
                ServerProperties p = Limbo.getInstance().getServerProperties();
                StatusPingEvent event = Limbo.getInstance().getEventsManager().callEvent(new StatusPingEvent(getClient(session), p.getVersionString(), p.getProtocol(), p.getMotd(), p.getMaxPlayers(), Limbo.getInstance().getPlayers().size(), p.getFavicon().orElse(null)));
                return new ServerStatusInfo(event.getMotd(),
                        new PlayerInfo(event.getMaxPlayers(), event.getPlayersOnline(), new ArrayList<>()),
                        new VersionInfo(event.getVersion(), event.getProtocol()),
                        event.getFavicon(), false
                );
            }
        });
    }

    public ServerSocket getServerSocket() {
        return serverSocket;
    }

    public Map<Session, ClientConnection> getClients() {
        return clients;
    }

    public ClientConnection getClient(Session session) {
        return clients.get(session);
    }
}
