/*
 * This file is part of Limbo.
 *
 * Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
 * Copyright (C) 2022. Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.loohp.limbo.utils;

import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandSender;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.data.game.command.properties.StringProperties;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

public class DeclareCommands {

    public static ClientboundCommandsPacket getDeclareCommandsPacket(CommandSender sender) {
        List<String> commands = Limbo.getInstance().getPluginManager().getTabOptions(sender, new String[0]);

        if (commands.isEmpty()) {
            return null;
        }

        List<CommandNode> nodes = new ArrayList<>();
        int[] childIndices = new int[commands.size()];
        for (int i = 0; i < commands.size(); i++) {
            childIndices[i] = i + 1;
        }
        nodes.add(new CommandNode(CommandType.ROOT, true, childIndices, OptionalInt.empty(), "", CommandParser.STRING, StringProperties.GREEDY_PHRASE, Key.key("ask_server")));
        for (String command : commands) {
            nodes.add(new CommandNode(CommandType.LITERAL, true, new int[0], OptionalInt.empty(), command, CommandParser.STRING, StringProperties.GREEDY_PHRASE, Key.key("ask_server")));
        }

        return new ClientboundCommandsPacket(nodes.toArray(new CommandNode[0]), 0);
    }

}
