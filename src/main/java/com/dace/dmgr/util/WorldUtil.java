package com.dace.dmgr.util;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.util.task.AsyncTask;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.commands.CommandManager;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

/**
 * 월드 관리 기능을 제공하는 클래스.
 */
@UtilityClass
public final class WorldUtil {
    /** SlimeWorld 플러그인 인스턴스 */
    private static final SWMPlugin swmPlugin = SWMPlugin.getInstance();
    /** SlimeWorld 설정 인스턴스 */
    private static final WorldsConfig worldsConfig = ConfigManager.getWorldConfig();

    /**
     * 지정한 월드로 새로운 복제본 월드를 생성한다.
     *
     * <p>복제본 월드의 이름은 언더바('_')로 시작해야 한다.</p>
     *
     * @param world           대상 월드
     * @param targetWorldName 복제본 월드 이름
     * @throws IllegalArgumentException {@code targetWorldName}이 언더바('_')로 시작하지 않으면 발생
     */
    @NonNull
    public static AsyncTask<@NonNull World> duplicateWorld(@NonNull World world, @NonNull String targetWorldName) {
        if (!targetWorldName.startsWith("_"))
            throw new IllegalArgumentException("'targetWorldName'이 '_'으로 시작해야 함");

        String worldName = world.getName();
        WorldData worldData = worldsConfig.getWorlds().get(worldName);
        String dataSource = worldData.getDataSource();
        SlimeLoader loader = swmPlugin.getLoader(dataSource);
        CommandManager.getInstance().getWorldsInUse().add(targetWorldName);

        return new AsyncTask<>((onFinish, onError) -> {
            try {
                SlimeWorld slimeWorld = swmPlugin.loadWorld(loader, worldName, true,
                        worldData.toPropertyMap()).clone(targetWorldName, loader);
                swmPlugin.generateWorld(slimeWorld);

                onFinish.accept(Bukkit.getWorld(targetWorldName));
            } catch (Exception ex) {
                ConsoleLogger.severe("월드 생성 실패 : {0}", ex, targetWorldName);
                onError.accept(ex);
            } finally {
                CommandManager.getInstance().getWorldsInUse().remove(targetWorldName);
            }
        });
    }

    /**
     * 지정한 월드를 삭제한다.
     *
     * @param world 삭제할 월드
     */
    @NonNull
    public static AsyncTask<Void> removeWorld(@NonNull World world) {
        String worldName = world.getName();
        Path path = Bukkit.getWorldContainer().toPath()
                .resolve(ConfigManager.getDatasourcesConfig().getFileConfig().getPath())
                .resolve(worldName + ".slime");

        return new AsyncTask<>((onFinish, onError) -> {
            try {
                Bukkit.unloadWorld(world, false);
                Files.delete(path);

                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("월드 삭제 실패 : {0}", ex, worldName);
                onError.accept(ex);
            } finally {
                CommandManager.getInstance().getWorldsInUse().remove(worldName);
            }
        });
    }

    /**
     * 모든 복제 월드를 삭제한다.
     *
     * @apiNote 비동기로 실행하지 않음. {@link AsyncTask}와 함께 사용하는 것을 권장
     */
    public static void clearDuplicatedWorlds() {
        File worldDir = new File(Bukkit.getWorldContainer(), ConfigManager.getDatasourcesConfig().getFileConfig().getPath());
        File[] worldFiles = worldDir.listFiles();
        if (worldFiles == null)
            return;

        Arrays.stream(worldFiles).filter(file -> file.getName().startsWith("_")).forEach(file -> {
            try {
                String worldName = file.getName().replace(".slime", "");
                World world = Bukkit.getWorld(worldName);
                if (world != null)
                    Bukkit.unloadWorld(world, false);

                Files.delete(file.toPath());
            } catch (Exception ex) {
                ConsoleLogger.severe("월드 삭제 중 오류 발생", ex);
            }
        });
    }
}
