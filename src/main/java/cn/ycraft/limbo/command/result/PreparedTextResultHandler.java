package cn.ycraft.limbo.command.result;

import cc.carm.lib.configuration.value.text.PreparedText;
import com.loohp.limbo.commands.CommandSender;
import dev.rollczi.litecommands.handler.result.ResultHandler;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;

public class PreparedTextResultHandler implements ResultHandler<CommandSender, PreparedText<?, CommandSender>> {
    @Override
    public void handle(Invocation<CommandSender> invocation, PreparedText<?, CommandSender> result,
                       ResultHandlerChain<CommandSender> chain) {
        result.to(invocation.sender());
    }
}
