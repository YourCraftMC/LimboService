package cn.ycraft.limbo.network.server;

import cn.ycraft.limbo.network.NetworkConstants;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.file.ServerProperties;
import com.loohp.limbo.utils.ForwardingUtils;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.cloudburstmc.nbt.NbtMap;
import org.geysermc.mcprotocollib.auth.GameProfile;
import org.geysermc.mcprotocollib.network.Session;
import org.geysermc.mcprotocollib.network.packet.Packet;
import org.geysermc.mcprotocollib.protocol.MinecraftConstants;
import org.geysermc.mcprotocollib.protocol.MinecraftProtocol;
import org.geysermc.mcprotocollib.protocol.ServerListener;
import org.geysermc.mcprotocollib.protocol.data.ProtocolState;
import org.geysermc.mcprotocollib.protocol.data.handshake.HandshakeIntent;
import org.geysermc.mcprotocollib.protocol.packet.handshake.serverbound.ClientIntentionPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundCustomQueryPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.clientbound.ClientboundHelloPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundCustomQueryAnswerPacket;
import org.geysermc.mcprotocollib.protocol.packet.login.serverbound.ServerboundHelloPacket;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.crypto.SecretKey;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.util.UUID;

public class LimboServerListener extends ServerListener {
    public LimboServerListener(NbtMap networkCodec) {
        super(networkCodec);
    }

    @Override
    public void packetReceived(Session session, Packet packet) {
        MinecraftProtocol protocol = (MinecraftProtocol) session.getPacketProtocol();
        if (protocol.getInboundState() == ProtocolState.LOGIN) {
            if (packet instanceof ServerboundHelloPacket) {
                ServerboundHelloPacket helloPacket = (ServerboundHelloPacket) packet;
                $setUsername(helloPacket.getUsername());
                boolean isVelocityModern = Limbo.getInstance().getServerProperties().isVelocityModern();

                if (isVelocityModern) {
                    session.send(new ClientboundCustomQueryPacket(session.getFlag(NetworkConstants.FORWARD_FLAG).id, Key.key("velocity", "player_info"), new byte[]{}));
                    return;
                }

                if (session.getFlag(MinecraftConstants.ENCRYPT_CONNECTION, true)) {
                    session.send(new ClientboundHelloPacket($getServerId(), $getKeyPair().getPublic(), $getChallenge(), session.getFlag(MinecraftConstants.SHOULD_AUTHENTICATE, true)));
                } else {
                    new Thread(() -> $auth(session, false, null)).start();
                }
                return;
            }
        }
        if (packet instanceof ServerboundCustomQueryAnswerPacket) {
            ServerboundCustomQueryAnswerPacket answerPacket = (ServerboundCustomQueryAnswerPacket) packet;
            if (answerPacket.getTransactionId() == session.getFlag(NetworkConstants.FORWARD_FLAG).id) {
                if (answerPacket.getData() == null) {
                    session.disconnect("Unknown login plugin response packet!");
                    return;
                }
                if (ForwardingUtils.validateVelocityModernResponse(answerPacket.getData())) {
                    session.getFlag(NetworkConstants.FORWARD_FLAG).velocityDataFrom = ForwardingUtils.getVelocityDataFrom(answerPacket.getData());
                    $auth(session, false, null);
                    return;
                }
                session.disconnect("Invalid playerinfo forwarding!");
                return;
            }
        }
        if (protocol.getInboundState() == ProtocolState.HANDSHAKE) {
            if (packet instanceof ClientIntentionPacket) {
                ServerProperties properties = Limbo.getInstance().getServerProperties();
                boolean isBungeecord = Limbo.getInstance().getServerProperties().isBungeecord();
                boolean isBungeeGuard = Limbo.getInstance().getServerProperties().isBungeeGuard();
                ClientIntentionPacket intentionPacket = (ClientIntentionPacket) packet;
                switch (intentionPacket.getIntent()) {
                    case STATUS: {
                        protocol.setOutboundState(ProtocolState.STATUS);
                        session.switchInboundState(() -> protocol.setInboundState(ProtocolState.STATUS));
                        break;
                    }
                    case LOGIN:
                    case TRANSFER:
                        String bungeeForwarding = intentionPacket.getHostname();
                        if (isBungeecord || isBungeeGuard) {
                            try {
                                String[] data = bungeeForwarding.split("\\x00");
                                String host = "";
                                String floodgate = "";
                                String clientIp = "";
                                String bungee = "";
                                String skinData = "";
                                int state = 0;
                                for (int i = 0; i < data.length; i++) {
                                    if (!properties.isReducedDebugInfo()) {
                                        Limbo.getInstance().getConsole().sendMessage(i + ": " + data[i]);
                                    }

                                    switch (state) {
                                        default:
                                            Limbo.getInstance().getConsole().sendMessage(i + ": ignore data: State: " + state);
                                            break;
                                        case 0:
                                            host = data[i];
                                            state = 1;
                                            break;
                                        case 1:
                                            if (data[i].startsWith("^Floodgate^")) {
                                                floodgate = data[i];
                                                state = 2;
                                                break;
                                            }
                                            /* fallthrough */
                                        case 2:
                                            clientIp = data[i];
                                            state = 3;
                                            break;
                                        case 3:
                                            bungee = data[i];
                                            state = 4;
                                            break;
                                        case 4:
                                            skinData = data[i];
                                            state = 6;
                                            break;
                                    }
                                }
                                if (state != 6) {
                                    throw new IllegalStateException("Illegal bungee state: " + state);
                                }

                                if (!properties.isReducedDebugInfo()) {
                                    Limbo.getInstance().getConsole().sendMessage("Host: " + host);
                                    Limbo.getInstance().getConsole().sendMessage("Floodgate: " + floodgate);
                                    Limbo.getInstance().getConsole().sendMessage("clientIp: " + clientIp);
                                    Limbo.getInstance().getConsole().sendMessage("bungee: " + bungee);
                                    Limbo.getInstance().getConsole().sendMessage("skinData: " + skinData);
                                }

                                session.getFlag(NetworkConstants.FORWARD_FLAG).uuid = UUID.fromString(bungee.replaceFirst("([0-9a-fA-F]{8})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]{4})([0-9a-fA-F]+)", "$1-$2-$3-$4-$5"));
                                session.getFlag(NetworkConstants.FORWARD_FLAG).inetAddress = InetAddress.getByName(clientIp);

                                boolean bungeeGuardFound = false;

                                if (!skinData.equals("")) {
                                    JSONArray skinJson = (JSONArray) new JSONParser().parse(skinData);

                                    for (Object obj : skinJson) {
                                        JSONObject property = (JSONObject) obj;
                                        Object signature = property.get("signature");
                                        session.getFlag(NetworkConstants.FORWARD_FLAG).properties.add(new GameProfile.Property(property.get("name").toString(), property.get("value").toString(), signature != null ? signature.toString() : null));
                                        if (isBungeeGuard && property.get("name").toString().equals("bungeeguard-token")) {
                                            String token = property.get("value").toString();
                                            bungeeGuardFound = Limbo.getInstance().getServerProperties().getForwardingSecrets().contains(token);
                                        }
                                    }
                                }

                                if (isBungeeGuard && !bungeeGuardFound) {
                                    session.disconnect("Invalid information forwarding");
                                    break;
                                }
                            } catch (Exception e) {
                                if (!properties.isReducedDebugInfo()) {
                                    StringWriter sw = new StringWriter();
                                    PrintWriter pw = new PrintWriter(sw);
                                    e.printStackTrace(pw);
                                    Limbo.getInstance().getConsole().sendMessage(sw.toString());
                                }
                                Limbo.getInstance().getConsole().sendMessage("If you wish to use bungeecord's IP forwarding, please enable that in your bungeecord config.yml as well!");
                                session.disconnect(Component.text("Please connect from the proxy!").color(NamedTextColor.RED));
                            }
                        }
                        if (intentionPacket.getIntent() == HandshakeIntent.TRANSFER) {
                            $beginLogin(session, protocol, intentionPacket, true);
                        } else if (intentionPacket.getIntent() == HandshakeIntent.LOGIN) {
                            $beginLogin(session, protocol, intentionPacket, false);
                        }
                        break;
                    default:
                        throw new UnsupportedOperationException("Invalid client intent: " + intentionPacket.getIntent());
                }
                return;
            }
        }
        super.packetReceived(session, packet);
    }

    private static final Method authMethod;
    private static final Method beginLoginMethod;
    private static final Field usernameField;
    private static final Field serverIdField;
    private static final Field keyPairField;
    private static final Field challengeField;

    static {
        try {
            authMethod = ServerListener.class.getDeclaredMethod("authenticate", Session.class, boolean.class, SecretKey.class);
            beginLoginMethod = ServerListener.class.getDeclaredMethod("beginLogin", Session.class, MinecraftProtocol.class, ClientIntentionPacket.class, boolean.class);
            usernameField = ServerListener.class.getDeclaredField("username");
            serverIdField = ServerListener.class.getDeclaredField("SERVER_ID");
            keyPairField = ServerListener.class.getDeclaredField("KEY_PAIR");
            challengeField = ServerListener.class.getDeclaredField("challenge");

            authMethod.setAccessible(true);
            beginLoginMethod.setAccessible(true);
            usernameField.setAccessible(true);
            serverIdField.setAccessible(true);
            keyPairField.setAccessible(true);
        } catch (NoSuchFieldException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private void $auth(Session session, boolean shouldAuthenticate, SecretKey key) {
        try {
            authMethod.invoke(this, session, shouldAuthenticate, key);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void $beginLogin(Session session, MinecraftProtocol protocol, ClientIntentionPacket packet, boolean transferred) {
        try {
            beginLoginMethod.invoke(this, session, protocol, packet, transferred);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private void $setUsername(String username) {
        try {
            usernameField.set(this, username);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public static String $getServerId() {
        try {
            return (String) serverIdField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static java.security.KeyPair $getKeyPair() {
        try {
            return (java.security.KeyPair) keyPairField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] $getChallenge() {
        try {
            return (byte[]) challengeField.get(null);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
