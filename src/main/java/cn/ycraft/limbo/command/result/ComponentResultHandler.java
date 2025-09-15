package cn.ycraft.limbo.command.result;

import com.loohp.limbo.commands.CommandSender;
import dev.rollczi.litecommands.handler.result.ResultHandler;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;
import net.kyori.adventure.text.Component;

public class ComponentResultHandler implements ResultHandler<CommandSender, Component> {
    @Override
    public void handle(Invocation<CommandSender> invocation, Component result,
                       ResultHandlerChain<CommandSender> chain) {
        invocation.sender().sendMessage(result);
    }
}
