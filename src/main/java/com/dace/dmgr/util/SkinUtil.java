package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import lombok.AllArgsConstructor;
import lombok.Getter;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * 플레이어 스킨 관리 기능을 제공하는 클래스.
 */
public final class SkinUtil {
    /** API 객체 */
    private static final SkinsRestorerAPI api = SkinsRestorerAPI.getApi();

    /**
     * 플레이어의 스킨을 변경한다.
     *
     * @param player 대상 플레이어
     * @param skin   스킨
     */
    public static void applySkin(Player player, Skin skin) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    api.applySkin(new PlayerWrapper(player), skin.getSkinName());
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

    /**
     * 지정한 스킨의 URL를 반환한다.
     *
     * @param skin 스킨
     * @return 스킨 전체 URL
     */
    public static String getSkinUrl(Skin skin) {
        return api.getSkinData(skin.getSkinName()).getValue();
    }

    /**
     * 지정할 수 있는 스킨의 목록.
     */
    @AllArgsConstructor
    @Getter
    public enum Skin {
        ARKACE("DVArkace"),
        JAGER("DVJager");

        /** 스킨 이름 */
        private final String skinName;
    }
}
