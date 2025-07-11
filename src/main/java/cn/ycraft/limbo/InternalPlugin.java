package cn.ycraft.limbo;

import cn.ycraft.limbo.command.LiteLimboFactory;
import cn.ycraft.limbo.command.defaults.*;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.file.FileConfiguration;
import com.loohp.limbo.plugins.LimboPlugin;

import java.io.File;
import java.io.IOException;

public class InternalPlugin extends LimboPlugin {

    {
        try {
            FileConfiguration config = new FileConfiguration(new File("internal-plugin.yml"));
            config.set("name", "LimboService");
            config.set("author", "YourCraftMC");
            config.set("version", Limbo.LIMBO_IMPLEMENTATION_VERSION);
            config.set("main", getClass().getName());
            setInfo(config, null);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void onEnable() {
        LiteLimboFactory.create(this)
            .commands(
                new AllowlistCommands(),
                new GameModeCommand(),
                new KickCommand(),
                new SayCommand(),
                new SpawnCommand(),
                new StopCommand(),
                new VersionCommand(),
                new DumpCommand()
            )
            .build();
    }
}
