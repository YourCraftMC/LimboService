package cn.ycraft.limbo.command;

import com.loohp.limbo.commands.CommandSender;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import dev.rollczi.litecommands.command.CommandRoute;
import dev.rollczi.litecommands.platform.AbstractPlatform;
import dev.rollczi.litecommands.platform.PlatformInvocationListener;
import dev.rollczi.litecommands.platform.PlatformSuggestionListener;
import org.jetbrains.annotations.NotNull;

public class LimboServicePlatform extends AbstractPlatform<CommandSender, LiteLimboSettings> {
    protected LimboServicePlatform(@NotNull LiteLimboSettings settings) {
        super(settings, LimboSender::new);
    }

    @Override
    protected void hook(CommandRoute<CommandSender> commandRoute, PlatformInvocationListener<CommandSender> invocationHook, PlatformSuggestionListener<CommandSender> suggestionHook) {
        LimboCommand<CommandSender> command = new LimboCommand<>(getSenderFactory(), getConfiguration(), commandRoute, invocationHook, suggestionHook);
        CommandDispatcher<CommandSender> dispatcher = InternalCommandRegistry.getDispatcher();
        LiteralCommandNode<CommandSender> commandNode = dispatcher.register(command.toLiteral());
        for (String alias : commandRoute.getAliases()) {
            dispatcher.register(LiteralArgumentBuilder.<CommandSender>literal(alias)
                    .requires(commandNode.getRequirement())
                    .redirect(commandNode));
        }
    }

    @Override
    protected void unhook(CommandRoute<CommandSender> commandRoute) {
    }
}
