package com.dace.dmgr;

import com.dace.dmgr.combat.event.CombatEventManager;
import com.dace.dmgr.event.MainEventManager;
import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.lobby.User;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.GameInfoRegistry;
import com.dace.dmgr.system.SystemPrefix;
import com.dace.dmgr.system.command.LobbyCommand;
import com.dace.dmgr.system.command.MenuCommand;
import com.dace.dmgr.system.command.PlayerOptionCommand;
import com.dace.dmgr.system.command.test.DummyCommand;
import com.dace.dmgr.system.command.test.GameTestCommand;
import com.dace.dmgr.system.command.test.SelectCharCommand;
import com.dace.dmgr.system.command.test.StatCommand;
import com.dace.dmgr.util.WorldUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

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
        getLogger().info("플러그인 활성화 완료");
        MainEventManager.init();
        CombatEventManager.init();
        WorldUtil.init();
        GameInfoRegistry.init();
        RankUtil.RankUpdater.init();
        registerCommands();
        registerTestCommands();

        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            User user = new User(player);
            user.init();
            getServer().broadcastMessage(SystemPrefix.CHAT + "플레이어 등록 : §e§n" + player.getName());
        });
        Bukkit.getOnlinePlayers().forEach((Player player) ->
                player.sendMessage(SystemPrefix.CHAT + "시스템 재부팅 완료"));
    }

    /**
     * 플러그인 비활성화 시 호출된다.
     */
    @Override
    public void onDisable() {
        getLogger().info("플러그인 비활성화 완료");

        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            User user = EntityInfoRegistry.getUser(player);
            if (user != null)
                user.getLobbySidebar().delete();
            player.sendMessage(SystemPrefix.CHAT + "시스템 재부팅 중...");
        });
    }

    /**
     * 모든 명령어를 등록한다.
     */
    private void registerCommands() {
        getCommand("스폰").setExecutor(new LobbyCommand());
        getCommand("메뉴").setExecutor(new MenuCommand());
        getCommand("설정").setExecutor(new PlayerOptionCommand());
    }

    /**
     * 모든 테스트용 명령어를 등록한다.
     */
    private void registerTestCommands() {
        getCommand("선택").setExecutor(new SelectCharCommand());
        getCommand("소환").setExecutor(new DummyCommand());
        getCommand("게임").setExecutor(new GameTestCommand());
        getCommand("스텟").setExecutor(new StatCommand());
    }
}