package cn.ycraft.limbo.command.result;

import cn.ycraft.limbo.config.value.ConfiguredMessage;
import com.loohp.limbo.commands.CommandSender;
import dev.rollczi.litecommands.handler.result.ResultHandler;
import dev.rollczi.litecommands.handler.result.ResultHandlerChain;
import dev.rollczi.litecommands.invocation.Invocation;

public class ConfiguredMessageResultHandler implements ResultHandler<CommandSender, ConfiguredMessage> {
    @Override
    public void handle(Invocation<CommandSender> invocation, ConfiguredMessage result,
                       ResultHandlerChain<CommandSender> chain) {
        result.sendTo(invocation.sender());
    }
}
