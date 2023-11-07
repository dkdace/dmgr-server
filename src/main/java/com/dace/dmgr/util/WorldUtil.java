package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.lobby.Lobby;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.commands.CommandManager;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.MessageFormat;

/**
 * 월드 관리 기능을 제공하는 클래스.
 */
public final class WorldUtil {
    /** 월드 저장 디렉토리 이름 */
    private static final String WORLD_DIRECTORY = "slime_worlds";

    /**
     * 모든 복제 월드를 삭제한다.
     */
    public static void clearDuplicatedWorlds() {
        File dir = new File(Bukkit.getWorldContainer(), WORLD_DIRECTORY);
        File[] worlds = dir.listFiles();
        if (worlds == null)
            return;

        for (File file : worlds) {
            if (file.getName().startsWith("_")) {
                try {
                    Files.delete(Paths.get(file.getPath()));
                    DMGR.getPlugin().getLogger().info(MessageFormat.format("복제된 월드 삭제 완료 : {0}", file.getName()));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    /**
     * 지정한 월드와 똑같은 새로운 월드를 생성한다.
     *
     * <p>복제본 월드의 이름은 언더바(_)로 시작해야 한다.</p>
     *
     * @param worldName      대상 월드 이름
     * @param duplicatedName 복제본 월드 이름
     */
    public static void duplicateWorld(String worldName, String duplicatedName) {
        if (!duplicatedName.startsWith("_"))
            return;

        World world = Bukkit.getWorld(duplicatedName);
        if (world != null)
            return;

        WorldsConfig config = ConfigManager.getWorldConfig();
        WorldData worldData = config.getWorlds().get(worldName);
        String dataSource = worldData.getDataSource();
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(dataSource);
        CommandManager.getInstance().getWorldsInUse().add(duplicatedName);

        Bukkit.getScheduler().runTaskAsynchronously(SWMPlugin.getInstance(), () -> {
            try {
                SlimeWorld slimeWorld = SWMPlugin.getInstance().loadWorld(loader, worldName, true,
                        worldData.toPropertyMap()).clone(duplicatedName, loader);
                SWMPlugin.getInstance().generateWorld(slimeWorld);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                CommandManager.getInstance().getWorldsInUse().remove(duplicatedName);
            }
        });

        DMGR.getPlugin().getLogger().info(MessageFormat.format("월드 복제 완료 : {0} -> {1}", worldName, duplicatedName));
    }

    /**
     * 지정한 월드를 비활성화한다.
     *
     * @param worldName 월드 이름
     */
    public static void unloadWorld(String worldName) {
        World world = Bukkit.getWorld(worldName);
        if (world == null)
            return;

        for (Player player : world.getPlayers())
            player.teleport(Lobby.lobbyLocation);

        if (Bukkit.unloadWorld(world, true)) {
            DMGR.getPlugin().getLogger().info(MessageFormat.format("월드 비활성화 완료 ({0})", worldName));
            CommandManager.getInstance().getWorldsInUse().remove(worldName);
        }
    }
}
