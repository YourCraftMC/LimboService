package cn.ycraft.limbo.config;

import cc.carm.lib.configuration.Configuration;
import cc.carm.lib.configuration.annotation.ConfigPath;
import cc.carm.lib.configuration.annotation.HeaderComments;
import cc.carm.lib.configuration.value.standard.ConfiguredList;
import cc.carm.lib.configuration.value.standard.ConfiguredValue;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;


@ConfigPath(root = true)
public interface AllowlistConfig extends Configuration {

    static boolean isAllowed(@Nullable UUID uuid, @Nullable String name) {
        if (uuid == null && name == null) return false;
        boolean containsName = name != null && BY_NAME.get().contains(name);
        boolean containsUUID = uuid != null && BY_UUID.get().contains(uuid);
        return REVERSED.resolve() != (containsName || containsUUID);
    }

    static int size() {
        return BY_NAME.get().size() + BY_UUID.get().size();
    }

    @HeaderComments({
        "Whether to reverse the allowlist,",
        " if true, will make this an exclusion list or \"banned list\".",
    })
    ConfiguredValue<Boolean> REVERSED = ConfiguredValue.of(true);

    @HeaderComments({
        "The list of players to allow or disallow.",
        " If the allowlist is reversed, players on this list will be disallowed.",
        " If the allowlist is not reversed, players not on this list will be disallowed."
    })
    ConfiguredList<UUID> BY_UUID = ConfiguredList.builderOf(UUID.class).fromString()
        .parse(UUID::fromString).serialize(UUID::toString)
        .defaults().build();

    @HeaderComments({
        "The list of players' name to allow or disallow.",
        " If the allowlist is reversed, players on this list will be disallowed.",
        " If the allowlist is not reversed, players not on this list will be disallowed."
    })
    ConfiguredList<String> BY_NAME = ConfiguredList.builderOf(String.class).fromString()
        .defaults().build();


}
