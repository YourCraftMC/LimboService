package com.loohp.limbo.utils;

import net.kyori.adventure.bossbar.BossBar;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarColor;
import org.geysermc.mcprotocollib.protocol.data.game.BossBarDivision;

public class BossBarUtil {
    public static BossBarDivision from(BossBar.Overlay overlay) {
        switch (overlay) {
            case NOTCHED_6:
                return BossBarDivision.NOTCHES_6;
            case NOTCHED_10:
                return BossBarDivision.NOTCHES_10;
            case NOTCHED_12:
                return BossBarDivision.NOTCHES_12;
            case NOTCHED_20:
                return BossBarDivision.NOTCHES_20;
            default:
                return BossBarDivision.NONE;
        }
    }

    public static BossBarColor color(BossBar.Color color) {
        switch (color) {
            case PINK:
                return BossBarColor.PINK;
            case RED:
                return BossBarColor.RED;
            case WHITE:
                return BossBarColor.WHITE;
            case YELLOW:
                return BossBarColor.YELLOW;
            case BLUE:
                return BossBarColor.CYAN;
            case GREEN:
                return BossBarColor.LIME;
            default:
                return BossBarColor.PURPLE;
        }
    }
}
