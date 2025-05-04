package com.dace.dmgr;

import com.dace.dmgr.command.CommandHandlerManager;
import com.dace.dmgr.event.EventListenerManager;
import com.dace.dmgr.game.Game;
import com.dace.dmgr.user.RankManager;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.EntityUtil;
import com.keenant.tabbed.Tabbed;
import lombok.NonNull;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

/**
 * 플러그인 메인 클래스.
 */
public class DMGR extends JavaPlugin {
    /** 탭리스트 관리 인스턴스 */
    @Nullable
    private static Tabbed tabbed;
    /** 홀로그램 관리 API 인스턴스 */
    @Nullable
    private static HolographicDisplaysAPI holographicDisplaysAPI;
    /** 스킨 관리 API 인스턴스 */
    @Nullable
    private static SkinsRestorerAPI skinsRestorerAPI;
    /** Citizens NPC 저장소 인스턴스 */
    @Nullable
    private static NPCRegistry npcRegistry;

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
     * @return 탭리스트 관리 인스턴스
     */
    @NonNull
    public static Tabbed getTabbed() {
        if (tabbed == null)
            tabbed = new Tabbed(DMGR.getPlugin());
        return tabbed;
    }

    /**
     * @return 홀로그램 관리 API 인스턴스
     */
    @NonNull
    public static HolographicDisplaysAPI getHolographicDisplaysAPI() {
        if (holographicDisplaysAPI == null)
            holographicDisplaysAPI = HolographicDisplaysAPI.get(DMGR.getPlugin());
        return holographicDisplaysAPI;
    }

    /**
     * @return 스킨 관리 API 인스턴스
     */
    @NonNull
    public static SkinsRestorerAPI getSkinsRestorerAPI() {
        if (skinsRestorerAPI == null)
            skinsRestorerAPI = SkinsRestorerAPI.getApi();
        return skinsRestorerAPI;
    }

    /**
     * @return Citizens NPC 저장소 인스턴스
     */
    @NonNull
    public static NPCRegistry getNpcRegistry() {
        if (npcRegistry == null)
            npcRegistry = CitizensAPI.createAnonymousNPCRegistry(new MemoryNPCDataStore());
        return npcRegistry;
    }

    /**
     * 플러그인 활성화 시 호출된다.
     */
    @Override
    public void onEnable() {
        GeneralConfig.getInstance().init()
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

        getHolographicDisplaysAPI().deleteHolograms();
        getNpcRegistry().deregisterAll();
    }
}