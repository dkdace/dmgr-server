package com.dace.dmgr;

import com.dace.dmgr.config.GeneralConfig;
import com.dace.dmgr.system.EventListener;
import com.dace.dmgr.system.PacketListener;
import com.dace.dmgr.system.command.LobbyCommand;
import com.dace.dmgr.system.command.MainMenuCommand;
import com.dace.dmgr.system.command.SelectCharCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class DMGR extends JavaPlugin {
    public static boolean debug = false;

    public static DMGR getPlugin() {
        return JavaPlugin.getPlugin(DMGR.class);
    }

    @Override
    public void onEnable() {
        getLogger().info(PREFIX.LOG + "플러그인 활성화 완료");
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        registerCommand();
        PacketListener.init();
        new GeneralConfig();
    }

    @Override
    public void onDisable() {
        getLogger().info(PREFIX.LOG + "플러그인 비활성화 완료");
    }

    private void registerCommand() {
        getCommand("스폰").setExecutor(new LobbyCommand());
        getCommand("메뉴").setExecutor(new MainMenuCommand());
        getCommand("선택").setExecutor(new SelectCharCommand());
    }

    public static class PREFIX {
        public final static String LOG = "[ DMGR-Core ] ";
        public final static String CHAT = "§b§l[ DMGR ] §f";
        public final static String CHAT_WARN = "§b§l[ DMGR ] §c";
    }
}