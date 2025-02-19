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

package cn.ycraft.limbo.network;

import cn.ycraft.limbo.config.ServerConfig;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.utils.BungeecordAdventureConversionUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.md_5.bungee.api.chat.BaseComponent;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.packet.common.clientbound.ClientboundCustomPayloadPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundLoginDisconnectPacket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.atomic.AtomicLong;

public class ClientConnection extends SessionAdapter {

    public static final String BRAND_ANNOUNCE_CHANNEL = Key.key("brand").toString();

    private final Session session;
    private AtomicLong lastPacketTimestamp;
    private SocketAddress inetAddress;

    public ClientConnection(Session session) {
        this.session = session;
        this.inetAddress = session.getRemoteAddress();
        this.lastPacketTimestamp = new AtomicLong(-1);
    }

    public SocketAddress getInetAddress() {
        return inetAddress;
    }

    public void setLastPacketTimestamp(long payLoad) {
        this.lastPacketTimestamp.set(payLoad);
    }

    public Player getPlayer() {
        return session.getFlag(NetworkConstants.PLAYER_FLAG);
    }


    public Session getSession() {
        return session;
    }

    public void sendPluginMessage(String channel, byte[] data) {
        sendPacket(new ClientboundCustomPayloadPacket(Key.key(channel), data));
    }

    public synchronized void sendPacket(Packet packet) {
        session.send(packet);
        setLastPacketTimestamp(System.currentTimeMillis());
    }

    public void disconnect(BaseComponent[] reason) {
        disconnect(BungeecordAdventureConversionUtils.toComponent(reason));
    }

    public void disconnect(Component reason) {
        session.disconnect(reason);
    }

    @Override
    public void packetSent(Session session, Packet packet) {
        if (packet instanceof ClientboundLoginDisconnectPacket) {
            ClientboundLoginDisconnectPacket disconnectPacket = (ClientboundLoginDisconnectPacket) packet;
            if (!ServerConfig.REDUCED_DEBUG_INFO.getNotNull()) {
                String str = (ServerConfig.LOG_PLAYER_IP_ADDRESSES.getNotNull() ? ((InetSocketAddress) inetAddress).getHostName() : "<ip address withheld>") + ":" + ((InetSocketAddress) session.getLocalAddress()).getPort();
                Limbo.getInstance().getConsole().sendMessage("[/" + str + "] <-> Player disconnected with the reason " + PlainTextComponentSerializer.plainText().serialize(disconnectPacket.getReason()));
            }
        }
    }

    public boolean isReady() {
        return session.hasFlag(NetworkConstants.PLAYER_FLAG);
    }
}
