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

import cn.ycraft.limbo.command.LiteLimboFactory;
import cn.ycraft.limbo.command.LiteLimboSettings;
import cn.ycraft.limbo.util.SchedulerUtils;
import com.loohp.limbo.Limbo;
import com.loohp.limbo.commands.CommandExecutor;
import com.loohp.limbo.commands.CommandSender;
import com.loohp.limbo.events.Event;
import com.loohp.limbo.events.Listener;
import com.loohp.limbo.file.FileConfiguration;
import dev.rollczi.litecommands.LiteCommandsBaseBuilder;
import dev.rollczi.litecommands.LiteCommandsBuilder;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class LimboPlugin {

    private String name;
    private File dataFolder;
    private PluginInfo info;
    private File pluginJar;
    private SchedulerUtils schedulerUtils;

    protected final void setInfo(FileConfiguration file, File pluginJar) {
        this.info = new PluginInfo(file);
        this.name = info.getName();
        this.dataFolder = new File(Limbo.getInstance().getPluginFolder(), name);
        this.pluginJar = pluginJar;
        this.schedulerUtils = new SchedulerUtils(this);
    }

    protected final File getPluginJar() {
        return pluginJar;
    }

    public void onLoad() {

    }

    public void onEnable() {

    }

    public void onDisable() {

    }

    public void registerListener(@NotNull Listener... listeners) {
        Arrays.stream(listeners).forEach(listener -> getServer().getEventsManager().registerEvents(this, listener));
    }

    public <B extends LiteCommandsBaseBuilder<CommandSender, LiteLimboSettings, B>> B commandBuilder() {
        return LiteLimboFactory.create(this);
    }

    public final String getName() {
        return name;
    }

    public final File getDataFolder() {
        return new File(dataFolder.getAbsolutePath());
    }

    public final PluginInfo getInfo() {
        return info;
    }

    public final Limbo getServer() {
        return Limbo.getInstance();
    }

    public final SchedulerUtils getScheduler() {
        return schedulerUtils;
    }

    public @NotNull <T> CompletableFuture<T> supplySync(@NotNull Supplier<T> action) {
        CompletableFuture<T> future = new CompletableFuture<>();
        getScheduler().run(() -> future.complete(action.get()));
        return future;
    }

    public @NotNull <T> CompletableFuture<T> supplyAsync(@NotNull Supplier<T> action) {
        CompletableFuture<T> future = new CompletableFuture<>();
        getScheduler().runAsync(() -> future.complete(action.get()));
        return future;
    }

    public @NotNull <T extends Event> CompletableFuture<T> callSync(T event) {
        return supplySync(() -> {
            getServer().getEventsManager().callEvent(event);
            return event;
        });
    }

    public @NotNull <T extends Event> CompletableFuture<T> callAsync(T event) {
        return supplyAsync(() -> {
            getServer().getEventsManager().callEvent(event);
            return event;
        });
    }

}
