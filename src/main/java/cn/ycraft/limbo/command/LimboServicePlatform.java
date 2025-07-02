package cn.ycraft.limbo.command;

import com.loohp.limbo.commands.CommandSender;
import com.mojang.brigadier.CommandDispatcher;
import dev.rollczi.litecommands.command.CommandRoute;
import dev.rollczi.litecommands.permission.PermissionService;
import dev.rollczi.litecommands.platform.AbstractSimplePlatform;
import dev.rollczi.litecommands.platform.PlatformInvocationListener;
import dev.rollczi.litecommands.platform.PlatformSuggestionListener;
import org.jetbrains.annotations.NotNull;

public class LimboServicePlatform extends AbstractSimplePlatform<CommandSender, LiteLimboSettings> {
    private final PermissionService permissionService;

    protected LimboServicePlatform(@NotNull LiteLimboSettings settings, final PermissionService permissionService) {
        super(settings, LimboSender::new);
        this.permissionService = permissionService;
    }

    @Override
    protected void hook(CommandRoute<CommandSender> commandRoute, PlatformInvocationListener<CommandSender> invocationHook, PlatformSuggestionListener<CommandSender> suggestionHook) {
        LimboCommand<CommandSender> command = new LimboCommand<>(getSenderFactory(), getConfiguration(), permissionService, commandRoute, invocationHook, suggestionHook);
        CommandDispatcher<CommandSender> dispatcher = InternalCommandRegistry.getDispatcher();
        dispatcher.register(command.toLiteral(commandRoute.getName()));
        for (String alias : commandRoute.getAliases()) {
            dispatcher.register(command.toLiteral(alias));
        }
    }

    @Override
    protected void unhook(CommandRoute<CommandSender> commandRoute) {
    }
}
