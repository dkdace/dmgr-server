package com.dace.dmgr;

import com.dace.dmgr.command.CommandHandlerManager;
import com.dace.dmgr.event.EventListenerManager;
import com.dace.dmgr.user.RankManager;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.keenant.tabbed.Tabbed;
import lombok.NonNull;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.MemoryNPCDataStore;
import net.citizensnpcs.api.npc.NPCRegistry;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.apache.commons.lang3.Validate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;

/**
 * 플러그인 메인 클래스.
 */
public class DMGR extends JavaPlugin {
    /** 일시적인 엔티티의 사용자 지정 이름 */
    public static final String TEMPORARY_ENTITY_CUSTOM_NAME = "temporary";
    /** 임시 복제 월드 이름의 접두사 */
    public static final String TEMPORARY_WORLD_NAME_PREFIX = "_";

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
                    clearUnusedEntities();
                    clearDuplicatedWorlds();

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
        if (npcRegistry != null)
            npcRegistry.deregisterAll();
    }

    /**
     * 사용되지 않는 모든 엔티티를 제거한다.
     */
    private void clearUnusedEntities() {
        Bukkit.getWorlds().stream()
                .flatMap(world -> world.getEntities().stream())
                .filter(entity -> entity.getCustomName() != null && entity.getCustomName().equals(TEMPORARY_ENTITY_CUSTOM_NAME))
                .forEach(Entity::remove);
    }

    /**
     * 모든 복제 월드를 삭제한다.
     */
    private void clearDuplicatedWorlds() {
        File worldDir = new File(Bukkit.getWorldContainer(), ConfigManager.getDatasourcesConfig().getFileConfig().getPath());
        File[] worldFiles = worldDir.listFiles();
        if (worldFiles == null)
            return;

        Arrays.stream(worldFiles)
                .filter(file -> file.getName().startsWith(TEMPORARY_WORLD_NAME_PREFIX))
                .forEach(file -> {
                    try {
                        World world = Bukkit.getWorld(file.getName().replace(".slime", ""));
                        if (world != null)
                            Bukkit.unloadWorld(world, false);

                        Files.delete(file.toPath());
                    } catch (Exception ex) {
                        ConsoleLogger.severe("월드 삭제 중 오류 발생", ex);
                    }
                });
    }
}