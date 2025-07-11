package cn.ycraft.limbo.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import cn.ycraft.limbo.config.data.Favicon;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.world.Environment;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

import java.util.Locale;
import java.util.Objects;

@ConfigPath(root = true)
public interface ServerConfig extends Configuration {

    @HeaderComments({
        "Statistics Settings",
        "This option is used to help developers count plug-in versions and usage, and it will never affect performance and user experience.",
        "Of course, you can also choose to turn it off here for this daemon."
    })
    ConfiguredValue<Boolean> METRICS = ConfiguredValue.of(true);

    interface SERVER extends Configuration {

        @HeaderComments("Server bind host, 'localhost' or '127.0.0.1' for local access only")
        ConfiguredValue<String> HOST = ConfiguredValue.of("0.0.0.0");

        @HeaderComments("Server port")
        ConfiguredValue<Integer> PORT = ConfiguredValue.of(25564);

        @HeaderComments("Ticks per second of the server, from 1 to 20, recommended 5.")
        ConfiguredValue<Integer> TPS = ConfiguredValue.of(5);

        @HeaderComments("Server max players, -1 for no limit.")
        ConfiguredValue<Integer> MAX_PLAYERS = ConfiguredValue.of(-1);

        @HeaderComments("Whether the server should be online mode.")
        ConfiguredValue<Boolean> ONLINE_MODE = ConfiguredValue.of(true);

        @HeaderComments("Server list version as string.")
        ConfiguredValue<String> VERSION = ConfiguredValue.of("Limbo!");

        @HeaderComments("Server list message in JSON.")
        ConfiguredValue<String> MOTD = ConfiguredValue.of("{\"text\":\"\",\"extra\":[{\"text\":\"Limbo Server!\",\"color\":\"yellow\"}]}");

        @HeaderComments("Server list favicon (May be left blank).")
        ConfiguredValue<Favicon> FAVICON = ConfiguredValue.builderOf(Favicon.class).fromString()
            .parse(Favicon::load).serialize(Favicon::path)
            .defaults(Favicon.load("server-icon.png")).build();

        interface TAB_LIST extends Configuration {
            @HeaderComments("Tab-List Footer (May be left blank)")
            ConfiguredValue<String> HEADER = ConfiguredValue.of("");

            @HeaderComments("Tab-List Header (May be left blank)")
            ConfiguredValue<String> FOOTER = ConfiguredValue.of("");
        }
    }

    @HeaderComments("Configuration for players' behavior")
    interface PLAYER extends Configuration {

        @HeaderComments({
            "The players' defaultGameMode.",
            "from: survival, creative, adventure, spectator"
        })
        ConfiguredValue<GameMode> DEFAULT_GAMEMODE = ConfiguredValue.builderOf(GameMode.class).fromString()
            .parse(str -> {
                try {
                    int i = Integer.parseInt(str);
                    return GameMode.byId(i);
                } catch (Exception e) {
                    return GameMode.valueOf(str.toUpperCase(Locale.ENGLISH));
                }
            })
            .serialize(Enum::name).defaults(GameMode.CREATIVE).build();

        @HeaderComments("The player's chat format, empty for disable chat functions")
        ConfiguredValue<String> CHAT_FORMAT = ConfiguredValue.of("<%(name)> %(message)");

        @HeaderComments("Whether Flying is allowed in limbo.")
        ConfiguredValue<Boolean> ALLOW_FLIGHT = ConfiguredValue.of(true);

        @HeaderComments("The view distance of the server")
        ConfiguredValue<Integer> VIEW_DISTANCE = ConfiguredValue.of(6);
        @HeaderComments({
            "If the proxy is enabled, the skin will be overwritten.",
            "If the proxy is turned off and this option is turned off, the skin will not be displayed"
        })
        ConfiguredValue<Boolean> OVERWRITE_SKIN = ConfiguredValue.of(true);


    }

    interface LOGS extends Configuration {

        @HeaderComments("Reduce debug info.")
        ConfiguredValue<Boolean> REDUCED_DEBUG_INFO = ConfiguredValue.of(true);

        @HeaderComments("Should a message be printed to the console when a handshake occurs.")
        ConfiguredValue<Boolean> HANDSHAKE_VERBOSE = ConfiguredValue.of(true);

        @HeaderComments("Should a message be printed to the console when a player connected to limbo.")
        ConfiguredValue<Boolean> CONNECTION_VERBOSE = ConfiguredValue.of(true);

        @HeaderComments({
            "Whether the IP addresses of players should be logged,",
            "If not enabled, players' IP addresses will be hidden in logs"
        })
        ConfiguredValue<Boolean> DISPLAY_IP_ADDRESS = ConfiguredValue.of(true);

    }

    interface PROXY extends Configuration {

        @HeaderComments({
            "Whether this server is behind a bungeecord proxy with BungeeGuard installed (velocity can do this too for <1.13)",
            "Mutually exclusive with bungeecord and velocity-modern."
        })
        ConfiguredValue<Boolean> BUNGEE_GUARD = ConfiguredValue.of(false);

        @HeaderComments({
            "Whether this server is behind a bungeecord proxy,",
            "Mutually exclusive with velocity-modern and bungee-guard."
        })
        ConfiguredValue<Boolean> BUNGEECORD = ConfiguredValue.of(false);

        @HeaderComments({
            "Whether this server is behind a velocity proxy with modern player forwarding,",
            "Mutually exclusive with BungeeCord and bungee-guard."
        })
        ConfiguredValue<Boolean> VELOCITY_MODERN = ConfiguredValue.of(false);

        @HeaderComments("For Velocity Modern Forwarding or BungeeGuard a list of valid secrets.")
        ConfiguredList<String> FORWARDING_SECRETS = ConfiguredList.builderOf(String.class)
            .fromString().defaults().build();


        static boolean usingProxy() {
            return BUNGEE_GUARD.resolve() || BUNGEECORD.resolve() || VELOCITY_MODERN.resolve();
        }

    }

    interface WORLD extends Configuration {

        @HeaderComments("World Name and the Schematic file containing map")
        ConfiguredValue<String> LEVEL = ConfiguredValue.of("world;spawn.schem");

        @HeaderComments({"Dimensions.", "Choose from: \"minecraft:overworld\", \"minecraft:the_nether\" or \"minecraft:the_end\""})
        ConfiguredValue<Environment> DIMENSION = ConfiguredValue.builderOf(Environment.class).fromString()
            .parse(s -> Objects.requireNonNull(Environment.fromKey(Key.key(s.toLowerCase(Locale.ENGLISH)))))
            .serialize(env -> env.getKey().asString())
            .defaults(Environment.END)
            .build();

        @HeaderComments("Spawn location")
        ConfiguredValue<Location> SPAWNPOINT = ConfiguredValue.builderOf(Location.class).fromSection()
            .parse(section -> new Location(
                null, // Will be set after the world is loaded
                section.getDouble("x", 0.0),
                section.getDouble("y", 0.0),
                section.getDouble("z", 0.0),
                section.getFloat("yaw", 0f),
                section.getFloat("pitch", 0f)
            )).serialize((holder, data, loc) -> {
                data.put("x", loc.getX());
                data.put("y", loc.getY());
                data.put("z", loc.getZ());
                if (loc.getPitch() != 0) data.put("pitch", loc.getPitch());
                if (loc.getYaw() != 0) data.put("yaw", loc.getYaw());
            }).defaults(new Location(null, 20.5, 17, 22.5, -90, 0)).build();

    }


    @HeaderComments("Settings for the server resource pack")
    interface RESOURCE_PACK extends Configuration {

        @HeaderComments("Whether to kick players if they do not accept the server resource pack")
        ConfiguredValue<Boolean> FORCE = ConfiguredValue.of(false);

        @HeaderComments("Server resource pack url (May be left blank)")
        ConfiguredValue<String> URL = ConfiguredValue.of("");

        @HeaderComments("JSON formatted text to show when prompting the player to install the resource pack (May be left blank)")
        ConfiguredValue<String> PROMPT = ConfiguredValue.of(
            "{\"text\"\\:\"\",\"extra\"\\:[{\"text\"\\:\"Install server resource pack\\!\",\"color\"\\:\"yellow\"}]}"
        );

        @HeaderComments("Server resource pack hash (May be left blank)")
        ConfiguredValue<String> SHA1 = ConfiguredValue.of("");

    }

    static String getSchematicFile() {
        return WORLD.LEVEL.resolve().split(";")[1];
    }

    static String getLevelName() {
        return WORLD.LEVEL.resolve().split(";")[0];
    }

}
