package cn.ycraft.limbo.network;

import com.loohp.limbo.player.Player;
import org.geysermc.mcprotocollib.network.Flag;

public class NetworkConstants {
    public static final Flag<Player> PLAYER_FLAG = new Flag<>("limbo:player", Player.class);
    public static final Flag<ClientConnection> CLIENT_CONNECTION_FLAG = new Flag<>("limbo:client_connection", ClientConnection.class);
}
