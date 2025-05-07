package cn.ycraft.limbo.command.defaults;

import cn.ycraft.limbo.command.DefaultCommands;
import cn.ycraft.limbo.config.AllowlistConfig;
import cn.ycraft.limbo.config.ServerMessages;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandSender;
import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.bind.Bind;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Sender;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;

import java.util.UUID;

@Command(name = "allowlist")
@Permission("limbo.command.allowlist")
public class AllowlistCommands implements DefaultCommands {

    @Execute(name = "toggle")
    void toggle(@Sender CommandSender sender) {
        boolean current = AllowlistConfig.REVERSED.resolve();
        AllowlistConfig.REVERSED.set(!current);
        if (AllowlistConfig.REVERSED.resolve()) {
            ServerMessages.ALLOWLIST.DENYING.sendTo(sender);
        } else {
            ServerMessages.ALLOWLIST.ALLOWING.sendTo(sender);
        }
    }

    @Execute(name = "reload")
    public void reload(@Sender CommandSender sender) {
        try {
            Limbo.getInstance().getAllowlistHolder().reload();
        } catch (Exception ignored) {
        }
        sendMessage(sender, ServerMessages.ALLOWLIST.RELOADED, AllowlistConfig.size());
    }

    @Execute(name = "add")
    public void addEntry(@Bind Limbo limbo, @Sender CommandSender sender, @Arg("uuid") UUID uuid) throws Exception {
        AllowlistConfig.BY_UUID.add(uuid);
        ServerMessages.ALLOWLIST.ADD.sendTo(sender, uuid.toString());

        limbo.getAllowlistHolder().save();
    }

    @Execute(name = "add")
    public void addEntry(@Bind Limbo limbo, @Sender CommandSender sender, @Arg("name") String name) throws Exception {
        AllowlistConfig.BY_NAME.add(name);
        ServerMessages.ALLOWLIST.ADD.sendTo(sender, name);

        limbo.getAllowlistHolder().save();
    }


    @Execute(name = "remove")
    public void removeEntry(@Bind Limbo limbo, @Sender CommandSender sender, @Arg("uuid") UUID uuid) throws Exception {
        AllowlistConfig.BY_UUID.remove(uuid);
        ServerMessages.ALLOWLIST.REMOVE.sendTo(sender, uuid.toString());

        limbo.getAllowlistHolder().save();
    }

    @Execute(name = "remove")
    public void removeEntry(@Bind Limbo limbo, @Sender CommandSender sender, @Arg("name") String name) throws Exception {
        AllowlistConfig.BY_NAME.removeIf(str -> str.equalsIgnoreCase(name));
        ServerMessages.ALLOWLIST.REMOVE.sendTo(sender, name);

        limbo.getAllowlistHolder().save();
    }

}
