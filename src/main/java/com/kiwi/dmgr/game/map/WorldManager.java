package com.kiwi.dmgr.game.map;

import com.dace.dmgr.DMGR;
import com.grinderwolf.swm.api.exceptions.*;
import com.grinderwolf.swm.api.loaders.SlimeLoader;
import com.grinderwolf.swm.api.world.SlimeWorld;
import com.grinderwolf.swm.plugin.SWMPlugin;
import com.grinderwolf.swm.plugin.commands.CommandManager;
import com.grinderwolf.swm.plugin.config.ConfigManager;
import com.grinderwolf.swm.plugin.config.WorldData;
import com.grinderwolf.swm.plugin.config.WorldsConfig;
import com.grinderwolf.swm.plugin.log.Logging;
import org.apache.commons.io.FilenameUtils;
import org.bukkit.*;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 * 월드 기능을 제공하는 클래스
 */
public class WorldManager {

    /**
     * 게임 맵 사용을 위해 복제된 월드를 삭제한다.
     */
    public static void init() {
        File dir = new File(Bukkit.getWorldContainer(), "slime_worlds");
        File[] worlds = dir.listFiles();

        if (worlds != null) {
            for (File file : worlds) {
                String name = FilenameUtils.removeExtension(file.getName());
                String[] names = name.split("_");
                try{
                    UUID uuid = UUID.fromString(names[1]);
                    if (file.delete())
                        DMGR.getPlugin().getLogger().info(DMGR.PREFIX.LOG + "복제 월드 삭제 완료 (" + file.getName() + ")");
                    else
                        DMGR.getPlugin().getLogger().info(DMGR.PREFIX.LOG + "복제 월드 삭제 실패 (" + file.getName() + ")");

                } catch (IllegalArgumentException ignored) { }
            }
        }
    }

    /**
     * 특정 맵의 슬라임 월드를 복제한다.
     *
     * @param loadWorld 복제할 월드
     * @param name 복제본 맵 이름
     * @return 복제 성공 여부
     */
    public static boolean generateWorld(String loadWorld, String name) {
        World world = Bukkit.getWorld(name);
        if (world != null) {
            return false;
        } else {
            WorldsConfig config = ConfigManager.getWorldConfig();
            WorldData worldData = config.getWorlds().get(loadWorld);
            if (worldData == null) {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to find world " + loadWorld + " inside the worlds config file.");
                return false;
            } else if (loadWorld.equals(name)) {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "The template world name cannot be the same as the cloned world one!");
                return false;
            } else if (CommandManager.getInstance().getWorldsInUse().contains(name)) {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "World " + name + " is already being used on another command! Wait some time and try again.");
                return false;
            } else {
                String dataSource = worldData.getDataSource();
                SlimeLoader loader = SWMPlugin.getInstance().getLoader(dataSource);
                CommandManager.getInstance().getWorldsInUse().add(name);
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.GRAY + "Creating world " + ChatColor.YELLOW + name + ChatColor.GRAY + " using " + ChatColor.YELLOW + loadWorld + ChatColor.GRAY + " as a template...");
                Bukkit.getScheduler().runTaskAsynchronously(SWMPlugin.getInstance(), () -> {
                    try {
                        long start = System.currentTimeMillis();
                        SlimeWorld slimeWorld = SWMPlugin.getInstance().loadWorld(loader, loadWorld, true, worldData.toPropertyMap()).clone(name, loader);
                        Bukkit.getScheduler().runTask(SWMPlugin.getInstance(), () -> {
                            try {
                                SWMPlugin.getInstance().generateWorld(slimeWorld);
                            } catch (IllegalArgumentException var6) {
                                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to generate world " + name + ": " + var6.getMessage() + ".");
                                return;
                            }

                            getLogger().info(Logging.COMMAND_PREFIX + ChatColor.GREEN + "World " + ChatColor.YELLOW + name + ChatColor.GREEN + " loaded and generated in " + (System.currentTimeMillis() - start) + "ms!");
                        });
                    } catch (WorldAlreadyExistsException var19) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "There is already a world called " + name + " stored in " + dataSource + ".");
                    } catch (CorruptedWorldException var20) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadWorld + ": world seems to be corrupted.");
                        Logging.error("Failed to load world " + loadWorld + ": world seems to be corrupted.");
                        var20.printStackTrace();
                    } catch (NewerFormatException var21) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadWorld + ": this world was serialized with a newer version of the Slime Format (" + var21.getMessage() + ") that SWM cannot understand.");
                    } catch (UnknownWorldException var22) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadWorld + ": world could not be found (using data source '" + worldData.getDataSource() + "').");
                    } catch (IllegalArgumentException var23) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadWorld + ": " + var23.getMessage());
                    } catch (IOException var24) {
                        getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to load world " + loadWorld + ". Take a look at the server console for more information.");
                        Logging.error("Failed to load world " + loadWorld + ":");
                        var24.printStackTrace();
                    } catch (WorldInUseException var25) {
                        throw new RuntimeException(var25);
                    } finally {
                        CommandManager.getInstance().getWorldsInUse().remove(name);
                    }

                });
                world = Bukkit.getWorld(name);
                if (world != null) {
                    DMGR.getPlugin().getLogger().info(DMGR.PREFIX.LOG + "월드 복제 완료 (" + world.getName() + ")");
                    return true;
                }
                else return false;
            }
        }
    }

    /**
     * 특정 슬라임 월드를 언로드한다.
     *
     * @param name 월드 이름
     * @return 월드 언로드 성공 여부
     */
    public static boolean unloadWorld(String name) {
        World world = Bukkit.getWorld(name);
        if (world == null) {
            getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "World " + name + " is not loaded!");
            return false;
        } else {
            List<Player> players = world.getPlayers();
            if (!players.isEmpty()) {
                World defaultWorld = Bukkit.getWorlds().get(0);
                Location spawnLocation = defaultWorld.getSpawnLocation();

                while (spawnLocation.getBlock().getType() != Material.AIR || spawnLocation.getBlock().getRelative(BlockFace.UP).getType() != Material.AIR) {
                    spawnLocation.add(0.0, 1.0, 0.0);
                }

                Iterator var7 = players.iterator();

                while (var7.hasNext()) {
                    Player player = (Player) var7.next();
                    player.teleport(spawnLocation);
                }
            }

            if (Bukkit.unloadWorld(world, true)) {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.GREEN + "World " + ChatColor.YELLOW + name + ChatColor.GREEN + " unloaded correctly.");
            } else {
                getLogger().info(Logging.COMMAND_PREFIX + ChatColor.RED + "Failed to unload world " + name + ".");
            }

            return true;
        }
    }
}
