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
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.Suggestions;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
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
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.TitlePart;
import org.jline.reader.Candidate;
import org.jline.reader.Completer;
import org.jline.reader.EndOfFileException;
import org.jline.reader.LineReader;
import org.jline.reader.LineReader.SuggestionType;
import org.jline.reader.LineReaderBuilder;
import org.jline.reader.ParsedLine;
import org.jline.reader.UserInterruptException;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

public class Console implements CommandSender {

    private final static String CONSOLE = "CONSOLE";
    private final static String PROMPT = "> ";
    protected final static String ERROR_RED = "\u001B[31;1m";
    protected final static String RESET_COLOR = "\u001B[0m";

    private final Terminal terminal;
    private final LineReader tabReader;
    private final LineReader reader;

    private final InputStream in;
    @SuppressWarnings("unused")
    private final PrintStream out;
    @SuppressWarnings("unused")
    private final PrintStream err;
    protected final PrintStream logs;

    public Console(InputStream in, PrintStream out, PrintStream err) throws IOException {
        String fileName = new SimpleDateFormat("yyyy'-'MM'-'dd'_'HH'-'mm'-'ss'_'zzz'.log'").format(new Date());
        File dir = new File("logs");
        dir.mkdirs();
        File logs = new File(dir, fileName);
        this.logs = new PrintStream(Files.newOutputStream(logs.toPath()), true, StandardCharsets.UTF_8);

        if (in != null) {
            System.setIn(in);
            this.in = System.in;
        } else {
            this.in = null;
        }
        System.setOut(new ConsoleOutputStream(this, out == null ? new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                //DO NOTHING
            }
        }) : out, this.logs));
        this.out = System.out;

        System.setErr(new ConsoleErrorStream(this, err == null ? new PrintStream(new OutputStream() {
            @Override
            public void write(int b) {
                //DO NOTHING
            }
        }) : err, this.logs));
        this.err = System.err;


        terminal = TerminalBuilder.builder().streams(in, out).jansi(true).dumb(false).build();
        reader = LineReaderBuilder.builder().terminal(terminal).build();
        tabReader = LineReaderBuilder.builder().terminal(terminal).completer(new Completer() {
            @Override
            public void complete(LineReader reader, ParsedLine line, List<Candidate> candidates) {
                com.mojang.brigadier.StringReader input = new StringReader(line.line());
                Suggestions suggestions = null;
                try {
                    suggestions = Limbo.getInstance().getPluginManager().suggest(Limbo.getInstance().getConsole(), input).get();
                } catch (InterruptedException | ExecutionException e) {
                    throw new RuntimeException(e);
                }
                for (Suggestion each : suggestions.getList()) {
                    candidates.add(new Candidate(each.getText()));
                }
            }
        }).build();
        tabReader.setAutosuggestion(SuggestionType.NONE);
    }

    @Override
    public String getName() {
        return CONSOLE;
    }

    @Override
    public boolean hasPermission(String permission) {
        return Limbo.getInstance().getPermissionsManager().hasPermission(this, permission);
    }


    @Override
    public void sendMessage(String message, UUID uuid) {
        sendMessage(message);
    }

    @Override
    public void sendMessage(Identity source, Component message, MessageType type) {
        sendComponent(message);
    }

    @Override
    public void openBook(Book book) {
        //ignore
    }

    @Override
    public void stopSound(SoundStop stop) {
        //ignore
    }

    @Override
    public void playSound(Sound sound, Emitter emitter) {
        //ignore
    }

    @Override
    public void playSound(Sound sound, double x, double y, double z) {
        //ignore
    }

    @Override
    public void playSound(Sound sound) {
        //ignore
    }

    @Override
    public void sendActionBar(Component message) {
        //ignore
    }

    @Override
    public void sendPlayerListHeaderAndFooter(Component header, Component footer) {
        //ignore
    }

    @Override
    public <T> void sendTitlePart(TitlePart<T> part, T value) {
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
    public void showBossBar(BossBar bar) {
        //ignore
    }

    @Override
    public void hideBossBar(BossBar bar) {
        //ignore
    }

    @Override
    public void sendMessage(String message) {
        stashLine();
        String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
        logs.println(ColorParser.clear("[" + date + " Info] " + message));
        reader.getTerminal().writer().append("[" + date + " Info] " + translateToConsole(message) + "\n");
        reader.getTerminal().flush();
        unstashLine();
    }

    public void sendComponent(Component message) {
        stashLine();
        String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
        logs.println(ColorParser.clear("[" + date + " Info] " + PlainTextComponentSerializer.plainText().serialize(message)));
        reader.getTerminal().writer().append("[" + date + " Info] " + translateToConsole(message) + "\n");
        reader.getTerminal().flush();
        unstashLine();
    }

    protected void run() {
        if (in == null) {
            return;
        }
        while (true) {
            try {
                String command = tabReader.readLine(PROMPT).trim();
                if (!command.isEmpty()) {
                    Limbo.getInstance().getScheduler().runTask(Limbo.getInstance().getPluginManager().getPlugin("LimboService"), () -> {
                        Limbo.getInstance().dispatchCommand(this, command);
                    });
                }
            } catch (UserInterruptException | EndOfFileException e) {
                break;
            }
        }
    }

    protected void stashLine() {
        try {
            tabReader.callWidget(LineReader.CLEAR);
        } catch (Exception ignore) {
        }
    }

    protected void unstashLine() {
        try {
            tabReader.callWidget(LineReader.REDRAW_LINE);
            tabReader.callWidget(LineReader.REDISPLAY);
            tabReader.getTerminal().writer().flush();
        } catch (Exception ignore) {
        }
    }

    protected static String translateToConsole(String str) {
        TextComponent component = LegacyComponentSerializer.legacySection().deserialize(str);
        return ANSIComponentSerializer.ansi().serialize(component);
    }

    protected static String translateToConsole(Component component) {
        return ANSIComponentSerializer.ansi().serialize(component);
    }

    public static class ConsoleOutputStream extends PrintStream {

        private final PrintStream logs;
        private final Console console;

        public ConsoleOutputStream(Console console, OutputStream out, PrintStream logs) {
            super(out);
            this.logs = logs;
            this.console = console;
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.printf(l, ColorParser.clear("[" + date + " Info]" + format), args);
            PrintStream stream = super.printf(l, Console.translateToConsole("[" + date + " Info]" + format), args);
            console.unstashLine();
            return stream;
        }

        @Override
        public PrintStream printf(String format, Object... args) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.printf(ColorParser.clear("[" + date + " Info]" + format), args);
            PrintStream stream = super.printf(ColorParser.clear("[" + date + " Info]" + format), args);
            console.unstashLine();
            return stream;
        }

        @Override
        public void println() {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]"));
            super.println(ColorParser.clear("[" + date + " Info]"));
            console.unstashLine();
        }

        @Override
        public void println(boolean x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + x));
            super.println(ColorParser.clear("[" + date + " Info]" + x));
            console.unstashLine();
        }

        @Override
        public void println(char x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + x));
            super.println(ColorParser.clear("[" + date + " Info]" + x));
            console.unstashLine();
        }

        @Override
        public void println(char[] x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + String.valueOf(x)));
            super.println(ColorParser.clear("[" + date + " Info]" + String.valueOf(x)));
            console.unstashLine();
        }

        @Override
        public void println(double x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + x));
            super.println(ColorParser.clear("[" + date + " Info]" + x));
            console.unstashLine();
        }

        @Override
        public void println(float x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + x));
            super.println(ColorParser.clear("[" + date + " Info]" + x));
            console.unstashLine();
        }

        @Override
        public void println(int x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + x));
            super.println(ColorParser.clear("[" + date + " Info]" + x));
            console.unstashLine();
        }

        @Override
        public void println(long x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + x));
            super.println(ColorParser.clear("[" + date + " Info]" + x));
            console.unstashLine();
        }

        @Override
        public void println(Object x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info]" + x));
            super.println(ColorParser.clear("[" + date + " Info]" + x));
            console.unstashLine();
        }

        @Override
        public void println(String string) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Info] " + string));
            super.println(ColorParser.clear("[" + date + " Info] " + string));
            console.unstashLine();
        }
    }

    public static class ConsoleErrorStream extends PrintStream {

        private final PrintStream logs;
        private final Console console;

        public ConsoleErrorStream(Console console, OutputStream out, PrintStream logs) {
            super(out);
            this.logs = logs;
            this.console = console;
        }

        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.printf(l, ColorParser.clear("[" + date + " Error]" + format), args);
            PrintStream stream = super.printf(l, ERROR_RED + ColorParser.clear("[" + date + " Error]" + format + RESET_COLOR), args);
            console.unstashLine();
            return stream;
        }

        @Override
        public PrintStream printf(String format, Object... args) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.printf(ColorParser.clear("[" + date + " Error]" + format), args);
            PrintStream stream = super.printf(ERROR_RED + ColorParser.clear("[" + date + " Error]" + format + RESET_COLOR), args);
            console.unstashLine();
            return stream;
        }

        @Override
        public void println() {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]"));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]") + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(boolean x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + x));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + x) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(char x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + x));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + x) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(char[] x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + String.valueOf(x)));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + String.valueOf(x)) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(double x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + x));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + x) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(float x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + x));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + x) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(int x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + x));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + x) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(long x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + x));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + x) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(Object x) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error]" + x));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error]" + x) + RESET_COLOR);
            console.unstashLine();
        }

        @Override
        public void println(String string) {
            console.stashLine();
            String date = new SimpleDateFormat("HH':'mm':'ss").format(new Date());
            logs.println(ColorParser.clear("[" + date + " Error] " + string));
            super.println(ERROR_RED + ColorParser.clear("[" + date + " Error] " + string) + RESET_COLOR);
            console.unstashLine();
        }
    }

}
