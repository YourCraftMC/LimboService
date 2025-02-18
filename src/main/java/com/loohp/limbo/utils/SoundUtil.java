package com.loohp.limbo.utils;

import com.google.common.primitives.SignedBytes;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.sound.SoundStop;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.BuiltinSound;
import org.geysermc.mcprotocollib.protocol.data.game.level.sound.SoundCategory;

public class SoundUtil {
    public static SoundCategory from(Sound.Source source) {
        switch (source) {
            case MASTER:
                return SoundCategory.MASTER;
            case MUSIC:
                return SoundCategory.MUSIC;
            case RECORD:
                return SoundCategory.RECORD;
            case WEATHER:
                return SoundCategory.WEATHER;
            case BLOCK:
                return SoundCategory.BLOCK;
            case HOSTILE:
                return SoundCategory.HOSTILE;
            case NEUTRAL:
                return SoundCategory.NEUTRAL;
            case PLAYER:
                return SoundCategory.PLAYER;
            case VOICE:
                return SoundCategory.VOICE;
            case AMBIENT:
                return SoundCategory.AMBIENT;
            default:
                throw new IllegalArgumentException();
        }
    }

    public static org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound from(Sound sound) {
        for (BuiltinSound builtinSound : BuiltinSound.values()) {
            if (sound.name().value().equalsIgnoreCase(builtinSound.getName())) {
                return builtinSound;
            }
        }
        return null;
    }

    public static org.geysermc.mcprotocollib.protocol.data.game.level.sound.Sound from(Key key) {
        for (BuiltinSound builtinSound : BuiltinSound.values()) {
            if (key.value().equalsIgnoreCase(builtinSound.getName())) {
                return builtinSound;
            }
        }
        return null;
    }
}
