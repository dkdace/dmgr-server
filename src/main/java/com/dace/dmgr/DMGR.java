package com.dace.dmgr;

import com.dace.dmgr.config.GeneralConfig;
import com.dace.dmgr.system.EventListener;
import com.dace.dmgr.system.command.LobbyCommand;
import com.dace.dmgr.system.command.MainMenuCommand;
import com.dace.dmgr.system.command.SelectCharCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public class DMGR extends JavaPlugin {
    public final static String LOG_PREFIX = "[ DMGR-Core ] ";
    public final static String CHAT_PREFIX = "§b§l[ DMGR ] §f";
    public final static String CHAT_WARN_PREFIX = "§b§l[ DMGR ] §c";
    public static boolean debug = false;

    public static DMGR getPlugin() {
        return JavaPlugin.getPlugin(DMGR.class);
    }

    @Override
    public void onEnable() {
        writeLog("플러그인 활성화 완료");
        getServer().getPluginManager().registerEvents(new EventListener(), this);
        registerCommand();
        new GeneralConfig();
    }

    @Override
    public void onDisable() {
        writeLog("플러그인 비활성화 완료");
    }

    private void registerCommand() {
        getCommand("스폰").setExecutor(new LobbyCommand());
        getCommand("메뉴").setExecutor(new MainMenuCommand());
        getCommand("선택").setExecutor(new SelectCharCommand());
    }

    public void writeLog(String msg) {
        this.getLogger().log(Level.INFO, LOG_PREFIX + msg);
    }

    public void writeLog(Level level, String msg) {
        this.getLogger().log(level, LOG_PREFIX + msg);
    }
}