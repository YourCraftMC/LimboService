package cn.ycraft.limbo.network.protocol;

import cn.ycraft.limbo.network.server.LimboServerListener;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.network.Server;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.crypt.EncryptionConfig;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;

import java.security.Key;

public class LimboProtocol extends MinecraftProtocol {
    private static NbtMap CODEC;

    @Override
    public void newServerSession(Server server, Session session) {
        resetStates();
        if (CODEC == null) {
            CODEC = NbtMap.EMPTY;
        }
        session.addListener(new LimboServerListener(CODEC));
    }

    @Override
    public EncryptionConfig createEncryption(Key key) {
        return super.createEncryption(key);
    }
}
