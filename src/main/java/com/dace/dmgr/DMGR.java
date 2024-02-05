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
import com.dace.dmgr.util.WorldUtil;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.Getter;
import lombok.NonNull;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

/**
 * 플러그인 메인 클래스.
 */
public class DMGR extends JavaPlugin {
    /** 난수 생성 객체 */
    @Getter
    private static final Random random = new Random();

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
        ConsoleLogger.info("플러그인 활성화 완료");

        GeneralConfig.getInstance().init().onFinish(this::loadUserDatas).onFinish(RankUtil::run);
        registerCommands();
        registerTestCommands();
        EventManager.register();
        clearUnusedEntities();
        WorldUtil.clearDuplicatedWorlds();

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
                .filter(entity -> entity.getType() == EntityType.ARMOR_STAND && entity.getCustomName() != null &&
                        entity.getCustomName().equals(User.NAME_TAG_HIDER_CUSTOM_NAME))
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
}