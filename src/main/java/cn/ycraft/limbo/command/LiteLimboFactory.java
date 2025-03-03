package cn.ycraft.limbo.command;

import cn.ycraft.limbo.command.argument.GameModeArgument;
import cn.ycraft.limbo.command.argument.PlayerArgument;
import cn.ycraft.limbo.command.context.LocationContext;
import cn.ycraft.limbo.command.context.SenderOnlyContextProvider;
import cn.ycraft.limbo.command.context.WorldContext;
import cn.ycraft.limbo.config.ServerMessages;
import cn.ycraft.limbo.config.value.ConfiguredMessage;
import cn.ycraft.limbo.util.SchedulerUtils;
import com.loohp.limbo.Console;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandSender;
import com.loohp.limbo.location.Location;
import com.loohp.limbo.player.Player;
import com.loohp.limbo.plugins.LimboPlugin;
import com.loohp.limbo.scheduler.LimboScheduler;
import com.loohp.limbo.world.World;
import dev.rollczi.litecommands.LiteCommandsBuilder;
import dev.rollczi.litecommands.LiteCommandsFactory;
import dev.rollczi.litecommands.message.LiteMessages;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;

public class LiteLimboFactory {
    private LiteLimboFactory() {
    }

    @SuppressWarnings("unchecked")
    public static <B extends LiteCommandsBuilder<CommandSender, LiteLimboSettings, B>> B create(LimboPlugin plugin) {
        return (B) LiteCommandsFactory.builder(CommandSender.class, new LimboServicePlatform(new LiteLimboSettings()))
                .self((builder, internal) -> {
                    builder.bind(Limbo.class, Limbo::getInstance)
                            .bind(LimboScheduler.class, () -> Limbo.getInstance().getScheduler())
                            .bind(SchedulerUtils.class, plugin::getScheduler)

                            .context(Player.class, new SenderOnlyContextProvider<>(Player.class, () -> ServerMessages.COMMAND.NOT_PLAYER))
                            .context(Console.class, new SenderOnlyContextProvider<>(Console.class, () -> ServerMessages.COMMAND.NOT_CONSOLE))
                            .context(World.class, new WorldContext(() -> ServerMessages.COMMAND.NOT_PLAYER))
                            .context(Location.class, new LocationContext(() -> ServerMessages.COMMAND.NOT_PLAYER))

                            .argument(Player.class, new PlayerArgument())
                            .argument(GameMode.class, new GameModeArgument())

                            .result(String.class, new StringResultHandler())
                            .result(ConfiguredMessage.class, new ConfiguredMessageResultHandler())

                            .message(LiteMessages.MISSING_PERMISSIONS, ServerMessages.NO_PERMISSION)
                    ;
                });
    }
}
