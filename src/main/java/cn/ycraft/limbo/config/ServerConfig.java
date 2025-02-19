package cn.ycraft.limbo.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import com.loohp.limbo.Console;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.events.status.StatusPingEvent;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.world.Environment;
import net.kyori.adventure.key.Key;
import org.checkerframework.checker.units.qual.C;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;

@ConfigPath(root = true)
public class ServerConfig implements Configuration {
    @HeaderComments({"Whether chat messages are allowed", "Setting this property to false should give more performance but will render the \"limboserver.chat\" permission powerless"})
    public static final ConfiguredValue<Boolean> ALLOW_CHAT = ConfiguredValue.of(true);
    @HeaderComments("Whether Flying is allowed")
    public static final ConfiguredValue<Boolean> ALLOW_FLIGHT = ConfiguredValue.of(true);
    @HeaderComments({"Whether this server is behind a bungeecord proxy with BungeeGuard installed (velocity can do this too for <1.13)", "Mutually exclusive with bungeecord and velocity-modern"})
    public static final ConfiguredValue<Boolean> BUNGEE_GUARD = ConfiguredValue.of(false);
    @HeaderComments({"Whether this server is behind a bungeecord proxy", "Mutually exclusive with velocity-modern and bungee-guard"})
    public static final ConfiguredValue<Boolean> BUNGEECORD = ConfiguredValue.of(false);
    @HeaderComments("GameMode, survival, creative, adventure, spectator")
    public static final ConfiguredValue<GameMode> DEFAULT_GAMEMODE = ConfiguredValue.builderOf(GameMode.class)
            .fromString()
            .parse(str -> {
                try {
                    int i = Integer.parseInt(str);
                    return GameMode.byId(i);
                } catch (Exception e) {
                    return GameMode.valueOf(str.toUpperCase(Locale.ENGLISH));
                }
            })
            .serialize(Enum::name)
            .defaults(GameMode.CREATIVE)
            .build();
    @HeaderComments("Whether to enforce the player whitelist. If true, enforces the whitelist from 'whitelist.json'")
    public static final ConfiguredValue<Boolean> ENFORCE_WHITELIST = ConfiguredValue.of(false);
    @HeaderComments("For Velocity Modern Forwarding or BungeeGuard a list (separated by `;`) of valid secrets")
    public static final ConfiguredList<String> FORWARDING_SECRETS = ConfiguredList.builderOf(String.class).fromString().defaults().build();
    @HeaderComments("Should a message be printed to the console when a handshake occurs")
    public static final ConfiguredValue<Boolean> HANDSHAKE_VERBOSE = ConfiguredValue.of(true);
    @HeaderComments("Dimension, \"minecraft:overworld\", \"minecraft:the_nether\" or \"minecraft:the_end\"")
    public static final ConfiguredValue<Environment> LEVEL_DIMENSION = ConfiguredValue.builderOf(Environment.class)
            .fromString()
            .defaults(Environment.NORMAL)
            .parse((s) -> Environment.fromKey(Key.key(s.toLowerCase(Locale.ENGLISH))))
            .serialize(env -> env.getKey().asString())
            .build();
    @HeaderComments("World Name and the Schematic file containing map")
    public static final ConfiguredValue<String> LEVEL_NAME = ConfiguredValue.of("world;spawn.schem");
    @HeaderComments({"Whether the IP addresseses of players should be logged", "If not enabled player IP addresses will be replaced by <ip address withheld> in logs"})
    public static final ConfiguredValue<Boolean> LOG_PLAYER_IP_ADDRESSES = ConfiguredValue.of(true);
    @HeaderComments("Server max players, -1 for no limit")
    public static final ConfiguredValue<Integer> MAX_PLAYERS = ConfiguredValue.of(-1);
    @HeaderComments("Server list message in Json")
    public static final ConfiguredValue<String> MOTD = ConfiguredValue.of("{\"text\":\"\",\"extra\":[{\"text\":\"Limbo Server!\",\"color\":\"yellow\"}]}");
    @HeaderComments("Whether the server should be online mode")
    public static final ConfiguredValue<Boolean> ONLINE_MODE = ConfiguredValue.of(true);
    @HeaderComments("Reduce debug info")
    public static final ConfiguredValue<Boolean> REDUCED_DEBUG_INFO = ConfiguredValue.of(true);
    @HeaderComments("Whether to kick players if they do not accept the server resource pack")
    public static final ConfiguredValue<Boolean> REQUIRED_RESOURCE_PACK = ConfiguredValue.of(false);
    @HeaderComments("Server resource pack url (May be left blank)")
    public static final ConfiguredValue<String> RESOURCE_PACK = ConfiguredValue.of("");
    @HeaderComments("JSON formatted text to show when prompting the player to install the resource pack (May be left blank)")
    public static final ConfiguredValue<String> RESOURCE_PACK_PROMPT = ConfiguredValue.of("{\"text\"\\:\"\",\"extra\"\\:[{\"text\"\\:\"Install server resource pack\\!\",\"color\"\\:\"yellow\"}]}");
    @HeaderComments("Server resource pack hash (May be left blank)")
    public static final ConfiguredValue<String> RESOURCE_PACK_SHA1 = ConfiguredValue.of("");
    @HeaderComments("Server ip, localhost for local access only")
    public static final ConfiguredValue<String> SERVER_IP = ConfiguredValue.of("0.0.0.0");
    @HeaderComments("Server port")
    public static final ConfiguredValue<Integer> SERVER_PORT = ConfiguredValue.of(30000);
    @HeaderComments("Tab-List Header (May be left blank)")
    public static final ConfiguredValue<String> TAB_FOOTER = ConfiguredValue.of("");
    @HeaderComments("Tab-List Footer (May be left blank)")
    public static final ConfiguredValue<String> TAB_HEADER = ConfiguredValue.of("");
    @HeaderComments("Ticks per second of the server")
    public static final ConfiguredValue<Integer> TICKS_PER_SECOND = ConfiguredValue.of(5);
    @HeaderComments({"Whether this server is behind a velocity proxy with modern player forwarding", "Mutually exclusive with bungeecord and bungee-guard"})
    public static final ConfiguredValue<Boolean> VELOCITY_MODERN = ConfiguredValue.of(false);
    @HeaderComments("Server list version as string")
    public static final ConfiguredValue<String> VERSION = ConfiguredValue.of("Limbo!");
    @HeaderComments("The view distance of the server")
    public static final ConfiguredValue<Integer> VIEW_DISTANCE = ConfiguredValue.of(6);
    @HeaderComments("Spawn location")
    public static final ConfiguredValue<Location> WORLD_SPAWN = ConfiguredValue.builderOf(Location.class)
            .fromString()
            .parse(str -> {
                String[] split = str.split(";");
                double x = 0, y = 0, z = 0;
                float yaw = 0, pitch = 0;
                x = Double.parseDouble(split[1]);
                y = Double.parseDouble(split[2]);
                z = Double.parseDouble(split[3]);
                if (split.length > 4) {
                    yaw = Float.parseFloat(split[4]);
                    pitch = Float.parseFloat(split[5]);
                }
                return new Location(Limbo.getInstance().getWorld(split[0]), x, y, z, yaw, pitch);
            })
            .serialize(loc -> {
                return (loc.getWorld() != null ? loc.getWorld().getName() : "world") + ";" + loc.getX() + ";" + loc.getY() + ";" + loc.getZ() + ";" + loc.getYaw() + ";" + loc.getPitch();
            })
            .defaults(new Location(null, 20.5, 17, 22.5, -90, 0))
            .build();
    public static byte[] FAVICON = null;

    public static Map<UUID, String> whitelist;

    public static String getSchemFileName() {
        return LEVEL_NAME.getNotNull().split(";")[1];
    }

    public static Key getLevelName() {
        return Key.key(LEVEL_NAME.getNotNull().split(";")[0]);
    }

    public static void reloadWhitelist() {
        Console console = Limbo.getInstance().getConsole();
        File whitelistFile = new File("whitelist.json");
        if (!whitelistFile.exists()) {
            try (PrintWriter pw = new PrintWriter(new OutputStreamWriter(Files.newOutputStream(whitelistFile.toPath())))) {
                pw.println("[]");
                pw.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        whitelist = new HashMap<>();
        try {
            JSONParser parser = new JSONParser();
            Object obj = parser.parse(new InputStreamReader(Files.newInputStream(whitelistFile.toPath()), StandardCharsets.UTF_8));

            if (!(obj instanceof JSONArray)) {
                console.sendMessage("whitelist: expected [] got {}");
                return;
            }

            JSONArray array = (JSONArray) obj;

            for (Object o : array) {
                if (!(o instanceof JSONObject)) {
                    console.sendMessage("whitelist: array element is not an object");
                    continue;
                }

                JSONObject element = (JSONObject) o;
                o = element.get("uuid");
                if (o == null) {
                    console.sendMessage("whitelist: missing uuid attribute");
                    continue;
                }
                if (!(o instanceof String)) {
                    console.sendMessage("whitelist: uuid is not a string");
                    continue;
                }

                String uuidStr = (String) o;
                UUID uuid = UUID.fromString(uuidStr);
                String name = element.containsKey("name") ? (String) element.get("name") : null;
                whitelist.put(uuid, name);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void reloadFavicon() {
        File png = new File("server-icon.png");
        if (png.exists()) {
            try {
                BufferedImage image = ImageIO.read(png);
                if (image.getHeight() == 64 && image.getWidth() == 64) {
                    FAVICON = Files.readAllBytes(png.toPath());
                } else {
                    Limbo.getInstance().getConsole().sendMessage("Unable to load server-icon.png! The image is not 64 x 64 in size!");
                }
            } catch (Exception e) {
                Limbo.getInstance().getConsole().sendMessage("Unable to load server-icon.png! Is it a png image?");
            }
        } else {
            Limbo.getInstance().getConsole().sendMessage("No server-icon.png found");
            FAVICON = null;
        }


    }
}
