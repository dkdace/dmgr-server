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
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
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
        GeneralConfig.getInstance().init()
                .onFinish(LobbyTabListProfile::loadSkins)
                .onFinish(GameTabListProfile::loadSkins)
                .onFinish(Dummy::loadSkin)
                .onFinish(CombatantType::loadSkins)
                .onFinish(UserData::initAllUserDatas)
                .onFinish(() -> {
                    Validate.notNull(RankManager.getInstance());
                    EventListenerManager.register();
                    CommandHandlerManager.register();
                    CommandHandlerManager.registerTestCommands();
                    EntityUtil.clearUnusedEntities();
                    Game.clearDuplicatedWorlds();

                    ConsoleLogger.info("플러그인 활성화 완료");

                    Bukkit.getOnlinePlayers().forEach(player -> User.fromPlayer(player).sendMessageInfo("시스템 재부팅 완료"));
                })
                .onError(ex -> ConsoleLogger.severe("플러그인 활성화 실패", ex));
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