package cn.ycraft.limbo.command.result;

import com.loohp.limbo.commands.CommandSender;
import dev.rollczi.litecommands.handler.result.ResultHandler;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;

public class StringResultHandler implements ResultHandler<CommandSender, String> {
    @Override
    public void handle(Invocation<CommandSender> invocation, String result, ResultHandlerChain<CommandSender> chain) {
        invocation.sender().sendMessage(result);
    }
}
