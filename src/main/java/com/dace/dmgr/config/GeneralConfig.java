package com.dace.dmgr.config;

import com.dace.dmgr.util.YamlUtil;

public class GeneralConfig {
    public static YamlUtil yamlUtil = new YamlUtil("GeneralConfig");
    public static String resourcePackUrl = "";
    public static int chatCooldown = 0;
    public static int commandCooldown = 0;

    public GeneralConfig() {
        resourcePackUrl = yamlUtil.loadValue("resourcePackUrl", resourcePackUrl);
        chatCooldown = yamlUtil.loadValue("chatCooldown", chatCooldown);
        commandCooldown = yamlUtil.loadValue("commandCooldown", commandCooldown);
    }
}
