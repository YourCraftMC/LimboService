package com.loohp.limbo.network;

import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.event.session.SessionAdapter;
import org.geysermc.mcprotocollib.network.packet.Packet;

public class ClientSessionAdapter extends SessionAdapter {
    @Override
    public void packetReceived(Session session, Packet packet) {

    }
}
