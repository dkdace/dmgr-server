package com.dace.dmgr;

import com.dace.dmgr.combat.event.CombatEventManager;
import com.dace.dmgr.config.GeneralConfig;
import com.dace.dmgr.event.MainEventManager;
import com.dace.dmgr.lobby.Lobby;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.command.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import static com.dace.dmgr.system.HashMapList.userMap;

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
        new GeneralConfig();
        registerCommand();

        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            User user = new User(player);
            userMap.put(player, user);
            Lobby.lobbyTick(player);
            getServer().broadcastMessage(PREFIX.CHAT + "플레이어 할당 : §e§n" + player.getName());
        });
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            player.sendMessage(PREFIX.CHAT + "시스템 재부팅 완료");
        });
    }

    @Override
    public void onDisable() {
        getLogger().info(PREFIX.LOG + "플러그인 비활성화 완료");
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            User user = new User(player);
            user.getLobbySidebar().delete();
            player.sendMessage(PREFIX.CHAT + "시스템 재부팅 중...");
        });
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