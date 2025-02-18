package cn.ycraft.limbo.network;

import cn.ycraft.limbo.network.server.ForwardData;
import com.loohp.limbo.player.Player;
import org.geysermc.mcprotocollib.network.Flag;

public class NetworkConstants {
    public static final Flag<Player> PLAYER_FLAG = new Flag<>("limbo:player", Player.class);
    public static final Flag<ClientConnection> CLIENT_CONNECTION_FLAG = new Flag<>("limbo:client_connection", ClientConnection.class);
    public static final Flag<ClientSessionPacketHandler> CLIENT_SESSION_PACKET_HANDLER_FLAG = new Flag<>("limbo:packet_handler", ClientSessionPacketHandler.class);
    public static final Flag<ForwardData> FORWARD_FLAG = new Flag<>("limbo:forward_data", ForwardData.class);
}
