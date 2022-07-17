package com.dace.dmgr.config;

import com.dace.dmgr.util.YamlModel;

public class GeneralConfig extends YamlModel {
    public static String resourcePackUrl;
    public static int chatCooldown;
    public static int commandCooldown;

    static {
        new GeneralConfig();
    }

    private GeneralConfig() {
        super("GeneralConfig");
        resourcePackUrl = loadValue("resourcePackUrl");
        chatCooldown = loadValue("chatCooldown");
        commandCooldown = loadValue("commandCooldown");
    }
}
