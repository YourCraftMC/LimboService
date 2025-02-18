package cn.ycraft.limbo.network.protocol;

import cn.ycraft.limbo.network.server.LimboServerListener;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.network.Server;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

public class LimboProtocol extends MinecraftProtocol {
    private static NbtMap CODEC;

    @Override
    public void newServerSession(Server server, Session session) {
        resetStates();
        if (CODEC == null) {
            CODEC = loadNetworkCodec();
        }
        session.addListener(new LimboServerListener(CODEC));
    }
}
