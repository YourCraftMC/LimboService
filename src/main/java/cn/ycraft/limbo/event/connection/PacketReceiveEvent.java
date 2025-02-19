package cn.ycraft.limbo.event.connection;

import cn.ycraft.limbo.network.ClientConnection;
import com.loohp.limbo.events.Cancellable;
import com.loohp.limbo.events.Event;
import org.geysermc.mcprotocollib.network.packet.Packet;


public class PacketReceiveEvent extends Event implements Cancellable {
    private boolean cancelled;
    private ClientConnection connection;
    private Packet packet;

    public PacketReceiveEvent(ClientConnection connection, Packet packet) {
        this.connection = connection;
        this.packet = packet;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public ClientConnection getConnection() {
        return connection;
    }

    public Packet getPacket() {
        return packet;
    }
}
