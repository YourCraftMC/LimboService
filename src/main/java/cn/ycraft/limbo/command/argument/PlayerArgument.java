package cn.ycraft.limbo.command.argument;

import cn.ycraft.limbo.config.ServerMessages;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandSender;
import com.loohp.limbo.player.Player;
import dev.rollczi.litecommands.argument.Argument;
import dev.rollczi.litecommands.argument.parser.ParseResult;
import dev.rollczi.litecommands.argument.resolver.ArgumentResolver;
import dev.rollczi.litecommands.invocation.Invocation;
import dev.rollczi.litecommands.suggestion.SuggestionContext;
import dev.rollczi.litecommands.suggestion.SuggestionResult;

public class PlayerArgument extends ArgumentResolver<CommandSender, Player> {
    @Override
    protected ParseResult<Player> parse(Invocation<CommandSender> invocation, Argument<Player> context, String argument) {
        Player player = Limbo.getInstance().getPlayer(argument);
        if (player != null) {
            return ParseResult.success(player);
        }
        return ParseResult.failure(ServerMessages.PLAYER_NOT_FOUND);
    }

    @Override
    public SuggestionResult suggest(Invocation<CommandSender> invocation, Argument<Player> argument, SuggestionContext context) {
        return Limbo.getInstance().getPlayers()
            .stream()
            .collect(SuggestionResult.collector(Player::getName, t -> t.getUniqueId().toString()));
    }
}
