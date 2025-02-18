package cn.ycraft.limbo.network.server;

import com.loohp.limbo.utils.ForwardingUtils;
import org.geysermc.mcprotocollib.auth.GameProfile;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class ForwardData {
    public final int id = ThreadLocalRandom.current().nextInt(1000000);
    public String username;
    public UUID uuid;
    public List<GameProfile.Property> properties = new ArrayList<>();
    public InetAddress inetAddress;
    public ForwardingUtils.VelocityModernForwardingData velocityDataFrom;
}
