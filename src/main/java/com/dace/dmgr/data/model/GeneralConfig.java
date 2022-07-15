package com.dace.dmgr.data.model;

import com.dace.dmgr.data.Model;

public class GeneralConfig extends Model {
    public static String resourcePackUrl;
    public static int chatCooldown;

    static {
        new GeneralConfig();
    }

    public GeneralConfig() {
        super("GeneralConfig");
        super.initConfig();
        resourcePackUrl = getConfigString("resourcePackUrl");
        chatCooldown = getConfigInt("chatCooldown");
    }

}
