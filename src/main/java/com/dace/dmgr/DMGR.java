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

/**
 * 플러그인 메인 클래스.
 */
public class DMGR extends JavaPlugin {
    /**
     * 플러그인 인스턴스를 반환한다.
     *
     * @return DMGR
     */
    public static DMGR getPlugin() {
        return JavaPlugin.getPlugin(DMGR.class);
    }

    /**
     * 플러그인 활성화 시 호출된다.
     */
    @Override
    public void onEnable() {
        getLogger().info(PREFIX.LOG + "플러그인 활성화 완료");
        GeneralConfig.init();
        MainEventManager.init();
        CombatEventManager.init();
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

    /**
     * 플러그인 비활성화 시 호출된다.
     */
    @Override
    public void onDisable() {
        getLogger().info(PREFIX.LOG + "플러그인 비활성화 완료");

        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            User user = new User(player);
            user.getLobbySidebar().delete();
            player.sendMessage(PREFIX.CHAT + "시스템 재부팅 중...");
        });
    }

    /**
     * 모든 명령어를 등록한다.
     */
    private void registerCommand() {
        getCommand("스폰").setExecutor(new LobbyCommand());
        getCommand("메뉴").setExecutor(new MenuCommand());
        getCommand("설정").setExecutor(new PlayerOptionCommand());
        getCommand("선택").setExecutor(new SelectCharCommand());
        getCommand("test").setExecutor(new TestCommand());
    }

    /**
     * 시스템 메시지의 접두사.
     */
    public static class PREFIX {
        /** 시스템 로그 */
        public final static String LOG = "[ DMGR-Core ] ";
        /** 일반 */
        public final static String CHAT = "§3§l[ §b§lDMGR §3§l] §f";
        /** 경고 */
        public final static String CHAT_WARN = "§3§l[ §b§lDMGR §3§l] §c";
    }
}