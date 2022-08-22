package com.dace.dmgr.system;

import com.dace.dmgr.DMGR;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class SkinManager {
    private static final SkinsRestorerAPI api = SkinsRestorerAPI.getApi();

    public static void applySkin(Player player, String skinName) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    api.applySkin(new PlayerWrapper(player), skinName);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(DMGR.getPlugin());
    }

    public static void resetSkin(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    api.applySkin(new PlayerWrapper(player), player.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }.runTaskAsynchronously(DMGR.getPlugin());
    }
}
