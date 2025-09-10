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

package com.loohp.limbo.plugins;

import cn.ycraft.limbo.InternalPlugin;
import cn.ycraft.limbo.command.InternalCommandRegistry;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandSender;
import com.loohp.limbo.file.FileConfiguration;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class PluginManager {

    private final Map<String, LimboPlugin> plugins;
    private final File pluginFolder;

    public PluginManager(File pluginFolder) {
        this.pluginFolder = pluginFolder;
        this.plugins = new LinkedHashMap<>();
    }

    protected void loadPlugins() {
        InternalPlugin internalPlugin = new InternalPlugin();
        plugins.put(internalPlugin.getName(), internalPlugin);
        for (File file : pluginFolder.listFiles()) {
            if (file.isFile() && file.getName().endsWith(".jar")) {
                boolean found = false;
                try (ZipInputStream zip = new ZipInputStream(new FileInputStream(file))) {
                    ZipEntry limboYmlEntry = null;
                    ZipEntry pluginYmlEntry = null;
                    // 首先遍历，优先查找 limbo.yml 和 plugin.yml 的入口
                    while (true) {
                        ZipEntry entry = zip.getNextEntry();
                        if (entry == null) {
                            break;
                        }
                        String name = entry.getName();
                        if (name.endsWith("limbo.yml")) {
                            limboYmlEntry = entry;
                            break; // 找到 limbo.yml 立即跳出
                        } else if (name.endsWith("plugin.yml")) {
                            pluginYmlEntry = entry;
                            // 不跳出，继续查找 limbo.yml
                        }
                    }
                    // 优先加载 limbo.yml
                    if (limboYmlEntry != null) {
                        found = true;
                        FileConfiguration pluginYaml = new FileConfiguration(zip);
                        String main = pluginYaml.get("main", String.class);
                        String pluginName = pluginYaml.get("name", String.class);
                        if (plugins.containsKey(pluginName)) {
                            System.err.println("Ambiguous plugin name in " + file.getName() + " with the plugin \"" + plugins.get(pluginName).getClass().getName() + "\"");
                        } else {
                            URLClassLoader child = new URLClassLoader(new URL[]{file.toURI().toURL()}, Limbo.getInstance().getClass().getClassLoader());
                            Class<?> clazz = Class.forName(main, true, child);
                            LimboPlugin plugin = (LimboPlugin) clazz.getDeclaredConstructor().newInstance();
                            plugin.setInfo(pluginYaml, file);
                            plugins.put(plugin.getName(), plugin);
                            plugin.onLoad();
                            Limbo.getInstance().getConsole().sendMessage("Loading plugin " + file.getName() + " " + plugin.getInfo().getVersion() + " by " + plugin.getInfo().getAuthor());
                        }
                    } else if (pluginYmlEntry != null) {
                        found = true;
                        FileConfiguration pluginYaml = new FileConfiguration(zip);
                        String main = pluginYaml.get("main", String.class);
                        String pluginName = pluginYaml.get("name", String.class);
                        if (plugins.containsKey(pluginName)) {
                            System.err.println("Ambiguous plugin name in " + file.getName() + " with the plugin \"" + plugins.get(pluginName).getClass().getName() + "\"");
                        } else {
                            URLClassLoader child = new URLClassLoader(new URL[]{file.toURI().toURL()}, Limbo.getInstance().getClass().getClassLoader());
                            Class<?> clazz = Class.forName(main, true, child);
                            LimboPlugin plugin = (LimboPlugin) clazz.getDeclaredConstructor().newInstance();
                            plugin.setInfo(pluginYaml, file);
                            plugins.put(plugin.getName(), plugin);
                            plugin.onLoad();
                            Limbo.getInstance().getConsole().sendMessage("Loading plugin " + file.getName() + " " + plugin.getInfo().getVersion() + " by " + plugin.getInfo().getAuthor());
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Unable to load plugin \"" + file.getName() + "\"");
                    e.printStackTrace();
                }
                if (!found) {
                    System.err.println("Jar file " + file.getName() + " has no plugin.yml or limbo.yml!");
                }
            }
        }
    }

    public List<LimboPlugin> getPlugins() {
        return new ArrayList<>(plugins.values());
    }

    public LimboPlugin getPlugin(String name) {
        return plugins.get(name);
    }

    public void dispatchCommand(CommandSender sender, String input) {
        if (input.isBlank()) return;

        Limbo.getInstance().getConsole().sendMessage(sender.getName() + " executed server command: /" + input);
        try {
            InternalCommandRegistry.execute(sender, input);
        } catch (CommandSyntaxException e) {
            sender.sendMessage(Component.text(e.getMessage()).color(NamedTextColor.RED));
        }
    }

    public CompletableFuture<Suggestions> suggest(CommandSender sender, StringReader input) {
        CommandDispatcher<CommandSender> dispatcher = InternalCommandRegistry.getDispatcher();
        ParseResults<CommandSender> parse = dispatcher.parse(input, sender);
        return dispatcher.getCompletionSuggestions(parse);
    }

    public File getPluginFolder() {
        return new File(pluginFolder.getAbsolutePath());
    }
}
