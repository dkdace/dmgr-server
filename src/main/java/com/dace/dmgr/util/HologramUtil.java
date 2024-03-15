package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;
import lombok.experimental.UtilityClass;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;

/**
 * 홀로그램 관리 기능을 제공하는 클래스.
 */
@UtilityClass
public final class HologramUtil {
    /** 생성된 홀로그램 목록 (홀로그램 ID : 홀로그램 객체) */
    private static final HashMap<String, Hologram> hologramMap = new HashMap<>();

    /**
     * 지정한 위치에 홀로그램을 생성한다.
     *
     * <p>이미 해당 ID의 홀로그램이 존재할 경우 덮어쓴다.</p>
     *
     * @param id       홀로그램 ID
     * @param location 생성할 위치
     * @param contents 내용 목록
     */
    public void addHologram(@NonNull String id, @NonNull Location location, @NonNull String... contents) {
        Hologram hologram = hologramMap.get(id);
        if (hologram == null)
            hologram = DMGR.getHolographicDisplaysAPI().createHologram(location);

        hologram.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.VISIBLE);
        hologram.getLines().clear();
        for (String content : contents) {
            hologram.getLines().appendText(content);
        }

        hologramMap.putIfAbsent(id, hologram);
    }

    /**
     * 홀로그램의 내용을 업데이트한다.
     *
     * <p>이미 해당 ID의 홀로그램이 존재할 경우 덮어쓴다.</p>
     *
     * @param id       홀로그램 ID
     * @param contents 내용 목록
     */
    public void editHologram(@NonNull String id, @NonNull String... contents) {
        Hologram hologram = hologramMap.get(id);
        if (hologram == null)
            return;

        if (contents.length < hologram.getLines().size())
            hologram.getLines().clear();

        for (int i = 0; i < contents.length; i++) {
            if (i < hologram.getLines().size())
                ((TextHologramLine) hologram.getLines().get(i)).setText(contents[i]);
            else
                hologram.getLines().appendText(contents[i]);
        }
    }

    /**
     * 지정한 플레이어의 홀로그램 표시 여부를 설정한다.
     *
     * @param id        홀로그램 ID
     * @param isVisible 표시 여부
     * @param player    대상 플레이어
     */
    public void setHologramVisibility(@NonNull String id, boolean isVisible, @NonNull Player player) {
        Hologram hologram = hologramMap.get(id);
        if (hologram == null)
            return;

        hologram.getVisibilitySettings().setIndividualVisibility(player, isVisible ?
                VisibilitySettings.Visibility.VISIBLE : VisibilitySettings.Visibility.HIDDEN);
    }

    /**
     * 지정한 플레이어의 홀로그램 표시 여부를 설정한다.
     *
     * @param id        홀로그램 ID
     * @param isVisible 표시 여부
     * @param players   대상 플레이어 목록
     */
    public void setHologramVisibility(@NonNull String id, boolean isVisible, @NonNull Player... players) {
        Hologram hologram = hologramMap.get(id);
        if (hologram == null)
            return;

        for (Player player : players) {
            hologram.getVisibilitySettings().setIndividualVisibility(player, isVisible ?
                    VisibilitySettings.Visibility.VISIBLE : VisibilitySettings.Visibility.HIDDEN);
        }
    }


    /**
     * 홀로그램을 지정한 엔티티에게 고정한다.
     *
     * @param id      홀로그램 ID
     * @param entity  고정할 엔티티. {@code null}로 지정 시 고정 해제
     * @param offsetX X 오프셋. (단위 : 블록)
     * @param offsetY Y 오프셋. (단위 : 블록)
     * @param offsetZ Z 오프셋. (단위 : 블록)
     */
    public void bindHologram(@NonNull String id, Entity entity, double offsetX, double offsetY, double offsetZ) {
        Hologram hologram = hologramMap.get(id);
        if (hologram == null)
            return;

        TaskUtil.clearTask(hologram);
        if (entity != null)
            TaskUtil.addTask(hologram, new IntervalTask(i -> {
                if (entity.isDead())
                    return false;

                hologram.setPosition(entity.getLocation().add(offsetX, offsetY, offsetZ));
                return true;
            }, isCancelled -> HologramUtil.removeHologram(id), 1));
    }

    /**
     * 홀로그램을 제거한다.
     *
     * @param id 홀로그램 ID
     */
    public void removeHologram(@NonNull String id) {
        Hologram hologram = hologramMap.get(id);
        if (hologram == null)
            return;

        hologram.delete();
        TaskUtil.clearTask(hologram);
        hologramMap.remove(id);
    }

    /**
     * 생성된 모든 홀로그램을 제거한다.
     */
    public void clearHologram() {
        hologramMap.forEach((id, hologram) -> {
            hologram.delete();
            TaskUtil.clearTask(hologram);
        });

        hologramMap.clear();
        DMGR.getHolographicDisplaysAPI().deleteHolograms();
    }
}
