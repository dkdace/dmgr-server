package com.dace.dmgr.effect;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.util.EntityUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import me.filoghost.holographicdisplays.api.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.apache.commons.lang3.Validate;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.LongConsumer;
import java.util.function.Predicate;

/**
 * 텍스트 홀로그램(가상 텍스트 디스플레이) 기능을 관리하는 클래스.
 */
public final class TextHologram {
    /** HolographicDisplays API 인스턴스 */
    private static final HolographicDisplaysAPI API = HolographicDisplaysAPI.get(DMGR.getPlugin());

    /** 틱 작업을 처리하는 태스크 */
    private final IntervalTask onTickTask;
    /** 홀로그램 인스턴스 */
    private final Hologram hologram;
    /** 홀로그램을 볼 수 있는 플레이어의 조건 */
    private final Predicate<Player> condition;

    /**
     * 지정한 위치에 홀로그램을 생성한다.
     *
     * @param location  생성할 위치
     * @param condition 홀로그램을 볼 수 있는 플레이어의 조건
     */
    public TextHologram(@NonNull Location location, @NonNull Predicate<@NonNull Player> condition) {
        this.hologram = API.createHologram(location);
        this.condition = condition;
        this.onTickTask = new IntervalTask((LongConsumer) i -> setVisibility(location.getWorld()), 1);
    }

    /**
     * 지정한 위치에 홀로그램을 생성한다.
     *
     * @param location  생성할 위치
     * @param condition 홀로그램을 볼 수 있는 플레이어의 조건
     * @param content   내용
     */
    public TextHologram(@NonNull Location location, @NonNull Predicate<@NonNull Player> condition, @NonNull String content) {
        this(location, condition);
        setContent(content);
    }

    /**
     * 지정한 엔티티의 머리 위치에 고정된 홀로그램을 생성한다.
     *
     * @param entity       고정할 엔티티
     * @param condition    홀로그램을 볼 수 있는 플레이어의 조건
     * @param heightOffset 높이 오프셋. (단위: 0.4+{@code heightOffset}×0.3블록)
     */
    public TextHologram(@NonNull Entity entity, @NonNull Predicate<@NonNull Player> condition, int heightOffset) {
        this.hologram = API.createHologram(entity.getLocation());
        this.condition = condition;

        this.onTickTask = new IntervalTask(i -> {
            if (!entity.isValid())
                return false;

            setVisibility(entity.getWorld());
            hologram.setPosition(entity.getLocation().add(0, entity.getHeight() + 0.4 + heightOffset * 0.3, 0));

            return true;
        }, 1);
    }

    /**
     * 지정한 엔티티의 머리 위치에 고정된 홀로그램을 생성한다.
     *
     * @param entity       고정할 엔티티
     * @param condition    홀로그램을 볼 수 있는 플레이어의 조건
     * @param heightOffset 높이 오프셋. (단위: 0.4+{@code heightOffset}×0.3블록)
     * @param content      내용
     */
    public TextHologram(@NonNull Entity entity, @NonNull Predicate<@NonNull Player> condition, int heightOffset, @NonNull String content) {
        this(entity, condition, heightOffset);
        setContent(content);
    }

    /**
     * 생성된 모든 API 홀로그램 인스턴스를 제거한한다.
     *
     * <p>플러그인 비활성화 시 호출해야 한다.</p>
     */
    public static void clearHologram() {
        API.deleteHolograms();
    }

    /**
     * 홀로그램의 내용을 설정한다.
     *
     * @param content 내용
     */
    public void setContent(@NonNull String content) {
        if (hologram.isDeleted())
            return;

        if (hologram.getLines().size() == 0)
            hologram.getLines().appendText(content);
        else
            ((TextHologramLine) hologram.getLines().get(0)).setText(content);
    }

    /**
     * 홀로그램의 플레이어별 표시 여부를 설정한다.
     *
     * @param world 현재 월드
     */
    private void setVisibility(@NonNull World world) {
        world.getPlayers().forEach(player -> {
            if (!EntityUtil.isCitizensNPC(player))
                hologram.getVisibilitySettings().setIndividualVisibility(player, condition.test(player)
                        ? VisibilitySettings.Visibility.VISIBLE
                        : VisibilitySettings.Visibility.HIDDEN);
        });
    }

    /**
     * 홀로그램을 제거한다.
     *
     * @throws IllegalStateException 이미 제거되었으면 발생
     */
    public void remove() {
        Validate.validState(!hologram.isDeleted(), "TextHologram이 이미 제거됨");

        hologram.delete();
        onTickTask.stop();
    }
}
