package cn.ycraft.limbo.command.argument;

import cn.ycraft.limbo.config.ServerMessages;
import com.loohp.limbo.commands.CommandSender;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invalidusage.InvalidUsage;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;
import org.geysermc.mcprotocollib.protocol.data.game.entity.player.GameMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Locale;

public class GameModeArgument extends ArgumentResolver<CommandSender, GameMode> {
    @Override
    protected ParseResult<GameMode> parse(Invocation<CommandSender> invocation, Argument<GameMode> context, String argument) {
        GameMode gameMode = parseGameMode(argument);
        if (gameMode != null) {
            return ParseResult.success(gameMode);
        }
        return ParseResult.failure(ServerMessages.GAMEMODE.AVAILABLE);
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<GameMode> argument, SuggestionContext context) {
        return Arrays.stream(GameMode.values())
            .collect(SuggestionResult.collector(g -> g.name().toLowerCase(Locale.ENGLISH), Enum::name));
    }

    public static @Nullable GameMode parseGameMode(@NotNull String mode) {
        if (mode.equalsIgnoreCase("survival") || mode.equalsIgnoreCase("s") || mode.equalsIgnoreCase("0")) {
            return GameMode.SURVIVAL;
        } else if (mode.equalsIgnoreCase("creative") || mode.equalsIgnoreCase("c") || mode.equalsIgnoreCase("1")) {
            return GameMode.CREATIVE;
        } else if (mode.equalsIgnoreCase("adventure") || mode.equalsIgnoreCase("a") || mode.equalsIgnoreCase("2")) {
            return GameMode.ADVENTURE;
        } else if (mode.equalsIgnoreCase("spectator") || mode.equalsIgnoreCase("sp") | mode.equalsIgnoreCase("3")) {
            return GameMode.SPECTATOR;
        }
        return null;
    }

}
