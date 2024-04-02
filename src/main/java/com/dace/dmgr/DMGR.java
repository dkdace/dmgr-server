package com.dace.dmgr;

import com.dace.dmgr.command.LobbyCommand;
import com.dace.dmgr.command.MenuCommand;
import com.dace.dmgr.command.PlayerOptionCommand;
import com.dace.dmgr.command.QuitCommand;
import com.dace.dmgr.command.test.*;
import com.dace.dmgr.event.EventManager;
import com.dace.dmgr.game.RankUtil;
import com.dace.dmgr.user.User;
import com.dace.dmgr.user.UserData;
import com.dace.dmgr.util.HologramUtil;
import com.dace.dmgr.util.WorldUtil;
import com.dace.dmgr.util.task.AsyncTask;
import com.keenant.tabbed.Tabbed;
import lombok.Getter;
import lombok.NonNull;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 플러그인 메인 클래스.
 */
public class DMGR extends JavaPlugin {
    /** 일시적인 엔티티의 사용자 지정 이름 */
    public static final String TEMPORAL_ENTITY_CUSTOM_NAME = "temporal";
    /** 난수 생성 객체 */
    @NonNull
    @Getter
    private static final Random random = new Random();
    /** 탭리스트 관리 객체 */
    private static Tabbed tabbed = null;
    /** 홀로그램 관리 객체 */
    private static HolographicDisplaysAPI holographicDisplaysAPI = null;
    /** 스킨 API 객체 */
    private static SkinsRestorerAPI skinsRestorerAPI = null;

    /**
     * 플러그인 인스턴스를 반환한다.
     *
     * @return DMGR
     */
    public static DMGR getPlugin() {
        return JavaPlugin.getPlugin(DMGR.class);
    }

    @NonNull
    public static Tabbed getTabbed() {
        if (tabbed == null)
            tabbed = new Tabbed(DMGR.getPlugin());
        return tabbed;
    }

    @NonNull
    public static HolographicDisplaysAPI getHolographicDisplaysAPI() {
        if (holographicDisplaysAPI == null)
            holographicDisplaysAPI = HolographicDisplaysAPI.get(DMGR.getPlugin());
        return holographicDisplaysAPI;
    }

    @NonNull
    public static SkinsRestorerAPI getSkinsRestorerAPI() {
        if (skinsRestorerAPI == null)
            skinsRestorerAPI = SkinsRestorerAPI.getApi();
        return skinsRestorerAPI;
    }

    /**
     * 서버의 최근 TPS (Ticks Per Second)를 반환한다.
     *
     * @return 최근 TPS
     */
    public static double getTps() {
        try {
            if (MinecraftServerNMS.minecraftServerClass == null)
                MinecraftServerNMS.minecraftServerClass = Class.forName("net.minecraft.server.v1_12_R1.MinecraftServer");

            if (MinecraftServerNMS.minecraftServer == null) {
                Method getServerMethod = MinecraftServerNMS.minecraftServerClass.getDeclaredMethod("getServer");
                getServerMethod.setAccessible(true);
                MinecraftServerNMS.minecraftServer = getServerMethod.invoke(null);
            }
            if (MinecraftServerNMS.recentTpsField == null) {
                MinecraftServerNMS.recentTpsField = MinecraftServerNMS.minecraftServerClass.getDeclaredField("recentTps");
                MinecraftServerNMS.recentTpsField.setAccessible(true);
            }

            double[] recent = (double[]) MinecraftServerNMS.recentTpsField.get(MinecraftServerNMS.minecraftServer);

            return recent[0];
        } catch (Exception ex) {
            ConsoleLogger.severe("서버 TPS를 구할 수 없음", ex);
        }

        return -1;
    }

    /**
     * 플러그인 활성화 시 호출된다.
     */
    @Override
    public void onEnable() {
        ConsoleLogger.info("플러그인 활성화 완료");

        GeneralConfig.getInstance().init();
        loadUserDatas().onFinish(RankUtil::run);
        EventManager.register();
        clearUnusedEntities();
        WorldUtil.clearDuplicatedWorlds();

        registerCommands();
        registerTestCommands();

        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            User user = User.fromPlayer(player);
            UserData.fromPlayer(player).init().onFinish(() -> user.sendMessageInfo("시스템 재부팅 완료"));
        });
    }

    /**
     * 플러그인 비활성화 시 호출된다.
     */
    @Override
    public void onDisable() {
        ConsoleLogger.info("플러그인 비활성화 완료");

        HologramUtil.clearHologram();
        Bukkit.getOnlinePlayers().forEach((Player player) -> {
            User user = User.fromPlayer(player);
            user.sendMessageInfo("시스템 재부팅 중...");
            user.dispose();
        });
    }

    /**
     * 접속했던 모든 플레이어의 유저 데이터를 불러온다.
     */
    @NonNull
    private AsyncTask<Void> loadUserDatas() {
        File dir = new File(DMGR.getPlugin().getDataFolder(), "User");
        File[] userDataFiles = dir.listFiles();
        if (userDataFiles == null)
            return new AsyncTask<>((onFinish, onError) -> onFinish.accept(null));

        List<AsyncTask<?>> userDataInitTasks = new ArrayList<>();

        for (File userDataFile : userDataFiles) {
            UUID uuid = UUID.fromString(FilenameUtils.removeExtension(userDataFile.getName()));
            UserData userData = UserData.fromUUID(uuid);

            if (!userData.isInitialized())
                userDataInitTasks.add(userData.init());
        }

        return AsyncTask.all(userDataInitTasks);
    }

    /**
     * 사용되지 않는 모든 엔티티를 제거한다.
     */
    private void clearUnusedEntities() {
        Bukkit.getWorlds().stream().flatMap(world -> world.getEntities().stream())
                .filter(entity -> entity.getCustomName() != null && entity.getCustomName().equals(TEMPORAL_ENTITY_CUSTOM_NAME))
                .forEach(Entity::remove);
    }

    /**
     * 모든 명령어를 등록한다.
     */
    private void registerCommands() {
        getCommand("스폰").setExecutor(new LobbyCommand());
        getCommand("메뉴").setExecutor(new MenuCommand());
        getCommand("설정").setExecutor(new PlayerOptionCommand());
        getCommand("퇴장").setExecutor(new QuitCommand());
    }

    /**
     * 모든 테스트용 명령어를 등록한다.
     */
    private void registerTestCommands() {
        getCommand("선택").setExecutor(new SelectCharCommand());
        getCommand("소환").setExecutor(new DummyCommand());
        getCommand("게임").setExecutor(new GameTestCommand());
        getCommand("랭크설정").setExecutor(new RankRateTestCommand());
        getCommand("경험치설정").setExecutor(new XpTestCommand());
    }

    /**
     * NMS 객체 관리 클래스.
     */
    private static class MinecraftServerNMS {
        /** 서버 NMS 클래스 */
        private static Class<?> minecraftServerClass;
        /** TPS 필드 객체 */
        private static Field recentTpsField;
        /** 서버 객체 */
        private static Object minecraftServer;
    }
}