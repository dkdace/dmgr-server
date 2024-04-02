package com.dace.dmgr.util;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.DMGR;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.skinsrestorer.api.PlayerWrapper;
import org.bukkit.entity.Player;

/**
 * 플레이어 스킨 관리 기능을 제공하는 클래스.
 */
@UtilityClass
public final class SkinUtil {
    /**
     * 플레이어의 스킨을 변경한다.
     *
     * @param player   대상 플레이어
     * @param skinName 적용할 스킨 이름
     */
    @NonNull
    public static AsyncTask<Void> applySkin(@NonNull Player player, @NonNull String skinName) {
        return new AsyncTask<>((onFinish, onError) -> {
            try {
                DMGR.getSkinsRestorerAPI().applySkin(new PlayerWrapper(player), skinName);
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("{0}의 스킨 적용 실패", ex, player.getName());
                onError.accept(ex);
            }
        });
    }

    /**
     * 플레이어의 스킨을 초기화한다.
     *
     * @param player 대상 플레이어
     */
    @NonNull
    public static AsyncTask<Void> resetSkin(@NonNull Player player) {
        return new AsyncTask<>((onFinish, onError) -> {
            try {
                DMGR.getSkinsRestorerAPI().applySkin(new PlayerWrapper(player), player.getName());
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("{0}의 스킨 초기화 실패", ex, player.getName());
                onError.accept(ex);
            }
        });
    }

    /**
     * 지정한 스킨 이름의 스킨 URL을 반환한다.
     *
     * @param skinName 스킨 이름
     * @return 스킨 URL
     */
    @NonNull
    public static String getSkinUrl(@NonNull String skinName) {
        return DMGR.getSkinsRestorerAPI().getSkinData(skinName).getValue();
    }
}
