package com.dace.dmgr.util;

import com.dace.dmgr.ConsoleLogger;
import com.dace.dmgr.util.task.AsyncTask;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import net.skinsrestorer.api.PlayerWrapper;
import net.skinsrestorer.api.SkinsRestorerAPI;
import org.bukkit.entity.Player;

/**
 * 플레이어 스킨 관리 기능을 제공하는 클래스.
 */
@UtilityClass
public final class SkinUtil {
    /** API 객체 */
    private static final SkinsRestorerAPI API = SkinsRestorerAPI.getApi();

    /**
     * 플레이어의 스킨을 변경한다.
     *
     * @param player 대상 플레이어
     * @param skin   적용할 스킨
     */
    @NonNull
    public static AsyncTask<Void> applySkin(@NonNull Player player, @NonNull Skin skin) {
        return new AsyncTask<>((onFinish, onError) -> {
            try {
                API.applySkin(new PlayerWrapper(player), skin.getSkinName());
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
                API.applySkin(new PlayerWrapper(player), player.getName());
                onFinish.accept(null);
            } catch (Exception ex) {
                ConsoleLogger.severe("{0}의 스킨 초기화 실패", ex, player.getName());
                onError.accept(ex);
            }
        });
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

        @NonNull
        public String getUrl() {
            return API.getSkinData(skinName).getValue();
        }
    }
}
