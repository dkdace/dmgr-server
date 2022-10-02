package com.dace.dmgr;

import com.dace.dmgr.combat.event.CombatEventManager;
import com.dace.dmgr.config.GeneralConfig;
import com.dace.dmgr.event.MainEventManager;
import com.dace.dmgr.system.PacketListener;
import com.dace.dmgr.system.command.*;
import org.bukkit.plugin.java.JavaPlugin;

public class DMGR extends JavaPlugin {
    public static boolean debug = false;

    public static DMGR getPlugin() {
        return JavaPlugin.getPlugin(DMGR.class);
    }

    @Override
    public void onEnable() {
        getLogger().info(PREFIX.LOG + "플러그인 활성화 완료");
        MainEventManager.init();
        CombatEventManager.init();
        PacketListener.init();
        registerCommand();
        new GeneralConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info(PREFIX.LOG + "플러그인 비활성화 완료");
    }

    private void registerCommand() {
        getCommand("스폰").setExecutor(new LobbyCommand());
        getCommand("메뉴").setExecutor(new MainMenuCommand());
        getCommand("설정").setExecutor(new OptionCommand());
        getCommand("선택").setExecutor(new SelectCharCommand());
    }

    public static class PREFIX {
        public final static String LOG = "[ DMGR-Core ] ";
        public final static String CHAT = "§3§l[ §b§lDMGR §3§l] §f";
        public final static String CHAT_WARN = "§3§l[ §b§lDMGR §3§l] §c";
    }
}