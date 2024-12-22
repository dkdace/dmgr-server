package com.dace.dmgr.util;

import com.dace.dmgr.DMGR;
import com.dace.dmgr.Disposable;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;
import me.filoghost.holographicdisplays.api.hologram.Hologram;
import me.filoghost.holographicdisplays.api.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.hologram.line.TextHologramLine;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.function.LongConsumer;
import java.util.function.Predicate;

/**
 * 텍스트 홀로그램(가상 텍스트 디스플레이) 기능을 관리하는 클래스.
 */
public final class TextHologram implements Disposable {
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
        this.hologram = DMGR.getHolographicDisplaysAPI().createHologram(location);
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
        this.hologram = DMGR.getHolographicDisplaysAPI().createHologram(entity.getLocation());
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
     * 홀로그램의 내용을 설정한다.
     *
     * @param content 내용
     */
    public void setContent(@NonNull String content) {
        validate();

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
        world.getPlayers().forEach(player ->
                hologram.getVisibilitySettings().setIndividualVisibility(player, condition.test(player)
                        ? VisibilitySettings.Visibility.VISIBLE
                        : VisibilitySettings.Visibility.HIDDEN));
    }

    /**
     * 홀로그램을 제거한다.
     */
    @Override
    public void dispose() {
        validate();

        hologram.delete();
        onTickTask.dispose();
    }

    @Override
    public boolean isDisposed() {
        return hologram.isDeleted();
    }
}
