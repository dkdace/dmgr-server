package com.dace.dmgr;

import com.dace.dmgr.combat.combatant.CombatantType;
import com.dace.dmgr.combat.entity.temporary.dummy.Dummy;
import com.dace.dmgr.combat.entity.temporary.spawnhandler.PlayerNPCSpawnHandler;
import com.dace.dmgr.command.CommandHandlerManager;
import com.dace.dmgr.effect.TextHologram;
import com.dace.dmgr.event.EventListenerManager;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.game.GameTabListProfile;
import com.dace.dmgr.user.LobbyTabListProfile;
import com.dace.dmgr.user.RankManager;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.ReflectionUtil;
import lombok.NonNull;
import org.bukkit.Bukkit;
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
    @NonNull
    public static DMGR getPlugin() {
        return JavaPlugin.getPlugin(DMGR.class);
    }

    /**
     * 플러그인 활성화 시 호출된다.
     */
    @Override
    public void onEnable() {
        try {
            ReflectionUtil.loadClass(GeneralConfig.class);
            ReflectionUtil.loadClass(LobbyTabListProfile.class);
            ReflectionUtil.loadClass(GameTabListProfile.class);
            ReflectionUtil.loadClass(Dummy.class);
            ReflectionUtil.loadClass(CombatantType.class);
            ReflectionUtil.loadClass(UserData.class);
            ReflectionUtil.loadClass(RankManager.class);
            ReflectionUtil.loadClass(EventListenerManager.class);
            ReflectionUtil.loadClass(CommandHandlerManager.class);
            ReflectionUtil.loadClass(EntityUtil.class);
            ReflectionUtil.loadClass(Game.class);

            Bukkit.getOnlinePlayers().forEach(player -> User.fromPlayer(player).sendMessageInfo("시스템 재부팅 완료"));

            ConsoleLogger.info("플러그인 활성화 완료");
        } catch (Exception ex) {
            ConsoleLogger.severe("플러그인 활성화 실패", ex);
        }
    }

    /**
     * 플러그인 비활성화 시 호출된다.
     */
    @Override
    public void onDisable() {
        ConsoleLogger.info("플러그인 비활성화 완료");

        Bukkit.getOnlinePlayers().forEach(player -> {
            User user = User.fromPlayer(player);
            user.sendMessageInfo("시스템 재부팅 중...");
            user.onQuit();
        });

        TextHologram.clearHologram();
        PlayerNPCSpawnHandler.clearNPC();
    }
}