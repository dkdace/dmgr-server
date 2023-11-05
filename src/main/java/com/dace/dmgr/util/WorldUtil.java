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
import org.apache.commons.io.FilenameUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.File;
import java.text.MessageFormat;
import java.util.UUID;

/**
 * 월드 관리 기능을 제공하는 클래스.
 */
public final class WorldUtil {
    /**
     * 지정한 월드 이름으로 생성한 무작위 월드 이름을 반환한다.
     *
     * @param worldName 월드 이름
     * @return 생성된 무작위 월드 이름
     */
    public static String getRandomWorldName(String worldName) {
        return worldName + "_" + UUID.randomUUID();
    }

    /**
     * 게임 맵 사용을 위해 복제된 월드를 삭제한다.
     */
    public static void init() {
        File dir = new File(Bukkit.getWorldContainer(), "slime_worlds");
        File[] worlds = dir.listFiles();
        if (worlds == null)
            return;

        for (File file : worlds) {
            String name = FilenameUtils.removeExtension(file.getName());
            if (name.split("_").length >= 2) {
                if (file.delete())
                    DMGR.getPlugin().getLogger().info(MessageFormat.format("복제 월드 삭제 완료 ({0})", file.getName()));
                else
                    DMGR.getPlugin().getLogger().info(MessageFormat.format("복제 월드 삭제 실패 ({0})", file.getName()));
            }
        }
    }

    /**
     * 특정 맵의 슬라임 월드를 복제한다.
     *
     * @param loadWorld 복제할 월드
     * @param name      복제본 맵 이름
     */
    public static void duplicateWorld(String loadWorld, String name) {
        World world = Bukkit.getWorld(name);
        if (world != null)
            return;

        WorldsConfig config = ConfigManager.getWorldConfig();
        WorldData worldData = config.getWorlds().get(loadWorld);
        String dataSource = worldData.getDataSource();
        SlimeLoader loader = SWMPlugin.getInstance().getLoader(dataSource);
        CommandManager.getInstance().getWorldsInUse().add(name);

        Bukkit.getScheduler().runTaskAsynchronously(SWMPlugin.getInstance(), () -> {
            try {
                SlimeWorld slimeWorld = SWMPlugin.getInstance().loadWorld(loader, loadWorld, true, worldData.toPropertyMap()).clone(name, loader);
                SWMPlugin.getInstance().generateWorld(slimeWorld);
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                CommandManager.getInstance().getWorldsInUse().remove(name);
            }
        });

        DMGR.getPlugin().getLogger().info(MessageFormat.format("월드 복제 완료 ({0})", name));
    }

    /**
     * 특정 슬라임 월드를 언로드한다.
     *
     * @param name 월드 이름
     */
    public static void unloadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null)
            return;

        for (Player player : world.getPlayers())
            player.teleport(Lobby.lobbyLocation);

        if (Bukkit.unloadWorld(world, true)) {
            DMGR.getPlugin().getLogger().info(MessageFormat.format("월드 삭제 완료 ({0})", name));
            CommandManager.getInstance().getWorldsInUse().remove(name);
        }
    }
}
