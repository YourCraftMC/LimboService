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

package com.loohp.limbo;

import cc.carm.lib.easyplugin.utils.ColorParser;
import com.loohp.limbo.commands.CommandSender;
import com.loohp.limbo.plugins.LimboPlugin;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import net.kyori.adventure.audience.MessageType;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.inventory.Book;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.Sound.Emitter;
import net.kyori.adventure.sound.SoundStop;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.serializer.ansi.ANSIComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.TitlePart;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.io.IoBuilder;
import org.jetbrains.annotations.NotNull;
import org.jline.reader.Candidate;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;

import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class Console extends SimpleTerminalConsole implements CommandSender {

    protected final static String CONSOLE_NAME = "CONSOLE";
    protected static final Logger logger = LogManager.getLogger(Console.class);

    public Console() {
        System.setOut(IoBuilder.forLogger(logger).setLevel(Level.INFO).buildPrintStream());
        System.setErr(IoBuilder.forLogger(logger).setLevel(Level.ERROR).buildPrintStream());
    }

    @Override
    public String getName() {
        return CONSOLE_NAME;
    }

    @Override
    public boolean hasPermission(String permission) {
        return Limbo.getInstance().getPermissionsManager().hasPermission(this, permission);
    }

    @Override
    protected LineReader buildReader(LineReaderBuilder builder) {
        return super.buildReader(builder.appName("LimboService")
            .completer((reader, line, candidates) -> {
                StringReader input = new StringReader(line.line());
                Suggestions suggestions;
                try {
                    suggestions = Limbo.getInstance().getPluginManager().suggest(this, input).get();
                } catch (InterruptedException | ExecutionException e) {
                    logger.error("Error occurred while getting tab completion", e);
                    return;
                }
                for (Suggestion each : suggestions.getList()) {
                    candidates.add(new Candidate(each.getText()));
                }
            })
        );
    }

    @Override
    protected boolean isRunning() {
        return Limbo.getInstance().isRunning();
    }

    @Override
    protected void runCommand(String command) {
        if (command.isEmpty()) return;
        LimboPlugin internal = Limbo.getInstance().getPluginManager().getPlugin("LimboService");
        Limbo.getInstance().getScheduler().runTaskAsync(
            internal, () -> Limbo.getInstance().dispatchCommand(this, command)
        );
    }

    @Override
    protected void shutdown() {
        Limbo.getInstance().stopServer();
    }

    @Override
    public void sendMessage(String message, UUID uuid) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(String message) {
        TextComponent component = LegacyComponentSerializer.legacySection().deserialize(ColorParser.parse(message));
        logger.info(ANSIComponentSerializer.ansi().serialize(component));
    }

    @Override
    @SuppressWarnings({"UnstableApiUsage", "deprecation"})
    public void sendMessage(@NotNull Identity source, @NotNull Component message, @NotNull MessageType type) {
        logger.info(ANSIComponentSerializer.ansi().serialize(message));
    }

    @Override
    public void openBook(@NotNull Book book) {
        //ignore
    }

    @Override
    public void stopSound(@NotNull SoundStop stop) {
        //ignore
    }

    @Override
    public void playSound(@NotNull Sound sound, @NotNull Emitter emitter) {
        //ignore
    }

    @Override
    public void playSound(@NotNull Sound sound, double x, double y, double z) {
        //ignore
    }

    @Override
    public void playSound(@NotNull Sound sound) {
        //ignore
    }

    @Override
    public void sendActionBar(@NotNull Component message) {
        //ignore
    }

    @Override
    public void sendPlayerListHeaderAndFooter(@NotNull Component header, @NotNull Component footer) {
        //ignore
    }

    @Override
    public <T> void sendTitlePart(@NotNull TitlePart<T> part, @NotNull T value) {
        //ignore
    }

    @Override
    public void clearTitle() {
        //ignore
    }

    @Override
    public void resetTitle() {
        //ignore
    }

    @Override
    public void showBossBar(@NotNull BossBar bar) {
        //ignore
    }

    @Override
    public void hideBossBar(@NotNull BossBar bar) {
        //ignore
    }

}
