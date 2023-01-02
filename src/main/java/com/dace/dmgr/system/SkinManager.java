package com.dace.dmgr.system;

import com.dace.dmgr.DMGR;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 플레이어 스킨 관리 기능을 제공하는 클래스.
 */
public class SkinManager {
    /** API 객체 */
    private static final SkinsRestorerAPI api = SkinsRestorerAPI.getApi();

    /**
     * 플레이어의 스킨을 변경한다.
     *
     * @param player   대상 플레이어
     * @param skinName 스킨 이름
     */
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

    /**
     * 플레이어의 스킨을 초기화한다.
     *
     * @param player 대상 플레이어
     */
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
