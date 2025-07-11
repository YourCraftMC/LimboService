package cn.ycraft.limbo.command;

import dev.rollczi.litecommands.platform.PlatformSettings;

public class LiteLimboSettings implements PlatformSettings {

    private String inputInspectionDisplay = "[...]";

    String getInputInspectionDisplay() {
        return this.inputInspectionDisplay;
    }

    public LiteLimboSettings inputInspectionDisplay(String name) {
        this.inputInspectionDisplay = name;
        return this;
    }

}
