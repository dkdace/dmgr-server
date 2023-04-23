package com.kiwi.dmgr.game.map;

import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.commands.CommandManager;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import com.grinderwolf.swm.plugin.log.Logging;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;

import java.io.IOException;
import java.util.UUID;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * 맵을 관리하는 클래스
 */
public class WorldManager {

    /**
     * 특정 맵 월드를 복사, 해당 복사본의 월드를 반환한다.
     *
     * <p> 월드의 이름은 맵 이름과 그 뒤에 UUID가 붙는다. </p>
     *
     * @param loadMap 불러올 맵
     * @return 복사본 맵 이름
     */
    public static String generateWorld(String loadMap) {
        String uuid = UUID.randomUUID().toString();
        final String worldName = loadMap + "_" + uuid;
        World world = Bukkit.getWorld(worldName);
        if (world != null) {
            return null;
        } else {
            WorldsConfig config = ConfigManager.getWorldConfig();
            WorldData worldData = config.getWorlds().get(loadMap);
            if (worldData == null) {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to find world " + loadMap + " inside the worlds config file.");
                return null;
            } else if (loadMap.equals(worldName)) {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "The template world name cannot be the same as the cloned world one!");
                return null;
            } else if (CommandManager.getInstance().getWorldsInUse().contains(worldName)) {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "World " + worldName + " is already being used on another command! Wait some time and try again.");
                return null;
            } else {
                String dataSource = worldData.getDataSource();
                SlimeLoader loader = SWMPlugin.getInstance().getLoader(dataSource);
                CommandManager.getInstance().getWorldsInUse().add(worldName);
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.GRAY + "Creating world " + ChatColor.YELLOW + worldName + ChatColor.GRAY + " using " + ChatColor.YELLOW + loadMap + ChatColor.GRAY + " as a template...");
                Bukkit.getScheduler().runTaskAsynchronously(SWMPlugin.getInstance(), () -> {
                    try {
                        long start = System.currentTimeMillis();
                        SlimeWorld slimeWorld = SWMPlugin.getInstance().loadWorld(loader, loadMap, true, worldData.toPropertyMap()).clone(worldName, loader);
                        Bukkit.getScheduler().runTask(SWMPlugin.getInstance(), () -> {
                            try {
                                SWMPlugin.getInstance().generateWorld(slimeWorld);
                            } catch (IllegalArgumentException var6) {
                                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to generate world " + worldName + ": " + var6.getMessage() + ".");
                                return;
                            }

                            getLogger().info(Logging.COMMAND_PREFIX + ChatColor.GREEN + "World " + ChatColor.YELLOW + worldName + ChatColor.GREEN + " loaded and generated in " + (System.currentTimeMillis() - start) + "ms!");
                        });
                    } catch (WorldAlreadyExistsException var19) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "There is already a world called " + worldName + " stored in " + dataSource + ".");
                    } catch (CorruptedWorldException var20) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadMap + ": world seems to be corrupted.");
                        Logging.error("Failed to load world " + loadMap + ": world seems to be corrupted.");
                        var20.printStackTrace();
                    } catch (NewerFormatException var21) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadMap + ": this world was serialized with a newer version of the Slime Format (" + var21.getMessage() + ") that SWM cannot understand.");
                    } catch (UnknownWorldException var22) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadMap + ": world could not be found (using data source '" + worldData.getDataSource() + "').");
                    } catch (IllegalArgumentException var23) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadMap + ": " + var23.getMessage());
                    } catch (IOException var24) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadMap + ". Take a look at the server console for more information.");
                        Logging.error("Failed to load world " + loadMap + ":");
                        var24.printStackTrace();
                    } catch (WorldInUseException var25) {
                        throw new RuntimeException(var25);
                    } finally {
                        CommandManager.getInstance().getWorldsInUse().remove(worldName);
                    }

                });
                world = Bukkit.getWorld(worldName);
                if (world != null) return worldName;
                else return null;
            }
        }
    }
}
