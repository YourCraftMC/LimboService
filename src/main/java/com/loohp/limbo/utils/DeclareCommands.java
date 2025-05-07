/*
  ~ This file is part of Limbo.
  ~
  ~ Copyright (C) 2024. YourCraftMC <admin@ycraft.cn>
  ~ Copyright (C) 2022. LoohpJames <jamesloohp@gmail.com>
  ~ Copyright (C) 2022. Contributors
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License");
  ~ you may not use this file except in compliance with the License.
  ~ You may obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  ~ See the License for the specific language governing permissions and
  ~ limitations under the License.
 */

package com.loohp.limbo.utils;

import cn.ycraft.limbo.command.InternalCommandRegistry;
import com.google.common.collect.Queues;
import com.loohp.limbo.commands.CommandSender;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.kyori.adventure.key.Key;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandNode;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandParser;
import org.geysermc.mcprotocollib.protocol.data.game.command.CommandType;
import org.geysermc.mcprotocollib.protocol.data.game.command.properties.StringProperties;
import org.geysermc.mcprotocollib.protocol.packet.ingame.clientbound.ClientboundCommandsPacket;

import java.util.List;
import java.util.OptionalInt;
import java.util.Queue;
import java.util.function.BiPredicate;

public class DeclareCommands {
    public static ClientboundCommandsPacket getDeclareCommandsPacket(CommandSender sender) {
        CommandDispatcher<CommandSender> dispatcher = InternalCommandRegistry.getDispatcher();
        RootCommandNode<CommandSender> root = new RootCommandNode<>();

        for (com.mojang.brigadier.tree.CommandNode<CommandSender> node : dispatcher.getRoot().getChildren()) {
            if (node.canUse(sender))
                root.addChild(node);
        }

        Object2IntMap<com.mojang.brigadier.tree.CommandNode<CommandSender>> object2IntMap = enumerateNodes(root);
        List<CommandNode> nodes = createEntries(object2IntMap);

        validateEntries(nodes);

        return new ClientboundCommandsPacket(nodes.toArray(new CommandNode[0]), object2IntMap.getInt(root));
    }

    private static void validateEntries(List<CommandNode> nodeDatas, BiPredicate<CommandNode, IntSet> validator) {
        IntSet intSet = new IntOpenHashSet(IntSets.fromTo(0, nodeDatas.size()));

        while (!intSet.isEmpty()) {
            boolean bl = intSet.removeIf(i -> validator.test(nodeDatas.get(i), intSet));
            if (!bl) {
                throw new IllegalStateException("Server sent an impossible command tree");
            }
        }
    }

    private static void validateEntries(List<CommandNode> nodeDatas) {
        validateEntries(nodeDatas, (a, indices) -> a.getRedirectIndex().isEmpty() || !indices.contains(a.getRedirectIndex().getAsInt()));
        validateEntries(nodeDatas, (a, indices) -> {
            for (int childIndex : a.getChildIndices()) {
                if (indices.contains(childIndex)) {
                    return false;
                }
            }
            return true;
        });
    }

    private static Object2IntMap<com.mojang.brigadier.tree.CommandNode<CommandSender>> enumerateNodes(RootCommandNode<CommandSender> commandTree) {
        Object2IntMap<com.mojang.brigadier.tree.CommandNode<CommandSender>> object2IntMap = new Object2IntOpenHashMap<>();
        Queue<com.mojang.brigadier.tree.CommandNode<CommandSender>> queue = Queues.newArrayDeque();
        queue.add(commandTree);

        com.mojang.brigadier.tree.CommandNode<CommandSender> commandNode;
        while ((commandNode = queue.poll()) != null) {
            if (!object2IntMap.containsKey(commandNode)) {
                int i = object2IntMap.size();
                object2IntMap.put(commandNode, i);
                queue.addAll(commandNode.getChildren());
                if (commandNode.getRedirect() != null) {
                    queue.add(commandNode.getRedirect());
                }
            }
        }

        return object2IntMap;
    }

    private static List<CommandNode> createEntries(Object2IntMap<com.mojang.brigadier.tree.CommandNode<CommandSender>> nodes) {
        ObjectArrayList<CommandNode> objectArrayList = new ObjectArrayList<>(nodes.size());
        objectArrayList.size(nodes.size());

        for (Object2IntMap.Entry<com.mojang.brigadier.tree.CommandNode<CommandSender>> entry : Object2IntMaps.fastIterable(nodes)) {
            objectArrayList.set(entry.getIntValue(), createEntry(entry.getKey(), nodes));
        }

        return objectArrayList;
    }

    private static CommandNode createEntry(
        com.mojang.brigadier.tree.CommandNode<CommandSender> node, Object2IntMap<com.mojang.brigadier.tree.CommandNode<CommandSender>> nodes
    ) {
        boolean isExecute = false;
        int redirect = 0;
        if (node.getRedirect() != null) {
            redirect = nodes.getInt(node.getRedirect());
        }

        if (node.getCommand() != null) {
            isExecute = true;
        }

        boolean customSuggestions = false;
        boolean isRoot = false;
        boolean isLiteral = false;
        boolean isArg = false;
        String name = "";
        if (node instanceof RootCommandNode) {
            isRoot = true;
        } else if (node instanceof ArgumentCommandNode<CommandSender, ?> argumentCommandNode) {
            isArg = true;
            if (argumentCommandNode.getCustomSuggestions() != null) {
                customSuggestions = true;
            }
            name = argumentCommandNode.getName();
        } else {
            if (!(node instanceof LiteralCommandNode<?> literalCommandNode)) {
                throw new UnsupportedOperationException("Unknown node type " + node);
            }
            isLiteral = true;
            name = literalCommandNode.getLiteral();
        }

        int[] is = node.getChildren().stream().mapToInt(nodes::getInt).toArray();
        CommandType type = null;
        if (isRoot) {
            type = CommandType.ROOT;
        }
        if (isLiteral) {
            type = CommandType.LITERAL;
        }
        if (isArg) {
            type = CommandType.ARGUMENT;
        }

        return new CommandNode(type, isExecute, is, redirect == 0 ? OptionalInt.empty() : OptionalInt.of(redirect), name, CommandParser.STRING, StringProperties.GREEDY_PHRASE, customSuggestions ? Key.key("ask_server") : null);
    }
}
