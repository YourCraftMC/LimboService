package cn.ycraft.limbo.command;

import com.loohp.limbo.Console;
import com.loohp.limbo.commands.CommandSender;
import com.loohp.limbo.player.Player;
import dev.rollczi.litecommands.identifier.Identifier;
import dev.rollczi.litecommands.platform.AbstractPlatformSender;

public class LimboSender extends AbstractPlatformSender {
    private final CommandSender sender;

    public LimboSender(CommandSender sender) {
        this.sender = sender;
    }

    @Override
    public String getName() {
        return sender.getName();
    }

    @Override
    public Identifier getIdentifier() {
        if (sender instanceof Player) {
            return Identifier.of(((Player) sender).getUniqueId());
        } else if (sender instanceof Console) {
            return Identifier.CONSOLE;
        }
        throw new UnsupportedOperationException("Unsupported sender type: " + sender.getClass().getName());
    }

    @Override
    public Object getHandle() {
        return sender;
    }
}
