package com.dace.dmgr.data.model;

import com.dace.dmgr.data.YamlModel;

public class GeneralConfig extends YamlModel {
    public static String resourcePackUrl;
    public static int chatCooldown;

    static {
        new GeneralConfig();
    }

    public GeneralConfig() {
        super("GeneralConfig");
        resourcePackUrl = getConfigString("resourcePackUrl");
        chatCooldown = getConfigInt("chatCooldown");
    }

}
