package com.dace.dmgr.config;

import com.dace.dmgr.util.YamlModel;

public class GeneralConfig extends YamlModel {
    public static String resourcePackUrl = "";
    public static int chatCooldown = 0;
    public static int commandCooldown = 0;

    public GeneralConfig() {
        super("GeneralConfig");
        resourcePackUrl = loadValue("resourcePackUrl", resourcePackUrl);
        chatCooldown = loadValue("chatCooldown", chatCooldown);
        commandCooldown = loadValue("commandCooldown", commandCooldown);
        saveConfig();
    }

    private void saveConfig() {
        saveValue("resourcePackUrl", resourcePackUrl);
        saveValue("chatCooldown", chatCooldown);
        saveValue("commandCooldown", commandCooldown);
    }
}
