package cn.ycraft.limbo.command.defaults;

import cn.ycraft.limbo.command.DefaultCommands;
import com.loohp.limbo.Console;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandSender;
import dev.rollczi.litecommands.annotations.bind.Bind;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Sender;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import dev.rollczi.litecommands.annotations.varargs.Varargs;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import static net.kyori.adventure.text.Component.text;

@Command(name = "say")
@Permission("limbo.command.say")
public class SayCommand implements DefaultCommands {

    @Execute
    public void execute(@Bind Limbo limbo, @Sender CommandSender sender, @Varargs("messages") String messages) throws Exception {
        String name = sender instanceof Console ? "Server" : sender.getName();
        Component message = text()
            .append(text("[").append(text(name)).append(text("] ")).color(NamedTextColor.GRAY))
            .append(text(messages))
            .build();

        limbo.sendMessage(message);
    }

}
