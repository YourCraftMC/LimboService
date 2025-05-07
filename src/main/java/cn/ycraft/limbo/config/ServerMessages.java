package cn.ycraft.limbo.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cn.ycraft.limbo.config.value.ConfiguredMessage;

@ConfigPath(root = true)
public interface ServerMessages extends Configuration {

    static ConfiguredMessage.Builder create() {
        return ConfiguredMessage.create();
    }

    ConfiguredMessage VERSION = create().defaults(
        "This server is running Limbo version %(version) (MC: %(implemented))"
    ).params("version", "implemented").build();


    ConfiguredMessage NOT_ALLOWED = create().defaults(
        "&cYou are not allowed to join this limbo!"
    ).build();

    ConfiguredMessage NO_PERMISSION = create().defaults(
        "&c&lSorry! &fBut you do not have permission to do this!"
    ).build();

    ConfiguredMessage PLAYER_NOT_FOUND = create().defaults(
        "&fPlayer is not online in this limbo!"
    ).build();

    ConfiguredMessage PLAYER_NOT_SPECIFY = create().defaults(
        "&fPlease specify a player!"
    ).build();

    interface CHAT extends Configuration {
        ConfiguredMessage DISALLOWED = create().defaults(
            "&c&lSorry! &fYou do not have permission to chat!"
        ).build();

        ConfiguredMessage DISABLED = create().defaults(
            "&c&lOh... &fSeems like no voices here..."
        ).build();
    }

    ConfiguredMessage TELEPORT_TO_SPAWN = create().defaults(
        "&aTeleporting to spawn..."
    ).build();

    interface KICK extends Configuration {
        ConfiguredMessage PLAYER = create().defaults(
            "&fKicked player &e%(player)&f from this limbo!"
        ).build();

        ConfiguredMessage REASON = create().defaults(
            "&fKicked player &e%(player)&f from this limbo for reason \"&f%(reason)&r\"&f!"
        ).params("reason").build();
    }

    interface ALLOWLIST extends Configuration {

        ConfiguredMessage RELOADED = create().defaults(
            "&fReloaded &e%(size) &fentries from the file!"
        ).params("size").build();

        ConfiguredMessage ADD = create().defaults(
            "&fAdded player &e%(player)&f to the list!"
        ).params("player").build();

        ConfiguredMessage REMOVE = create().defaults(
            "&fRemoved player &e%(player)&f from the list!"
        ).params("player").build();

        ConfiguredMessage ALLOWING = create().defaults(
            "&fCurrently using the &aallowlist&f!"
        ).build();

        ConfiguredMessage DENYING = create().defaults(
            "&fCurrently using the &cdenylist&f!"
        ).build();
    }

    interface GAMEMODE extends Configuration {
        ConfiguredMessage SET_PLAYER = create().defaults(
            "&fSet player &e%(player)&f's game mode to &e%(mode)&f!"
        ).params("player", "mode").build();

        ConfiguredMessage SET_SELF = create().defaults(
            "&fYour game mode has been updated to &e%(mode)&f!"
        ).params("mode").build();

        ConfiguredMessage AVAILABLE = create().defaults(
            "&7Available modes: &fsurvival&7, &fcreative&7, &fadventure&7, &fspectator&7"
        ).build();
    }

    interface COMMAND extends Configuration {

        ConfiguredMessage UNKNOWN = create().defaults(
            "&c&lUnknown command! &fPlease check and try again."
        ).build();

        ConfiguredMessage NO_PERMISSION = create().defaults(
            "&c&lSorry! &fBut you do not have permission to do this!"
        ).build();

        ConfiguredMessage NOT_PLAYER = create().defaults(
            "&c&lSorry! &fBut you must be a player to do this!"
        ).build();

        ConfiguredMessage NOT_CONSOLE = create().defaults(
            "&c&lSorry! &fBut you must be a console to do this!"
        ).build();
    }

}
