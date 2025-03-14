package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.Nullable;

import java.text.MessageFormat;

/**
 * 무기의 2중 무기 모듈 클래스.
 *
 * @param <T> {@link Weapon}을 상속받는 보조무기
 * @see Swappable
 */
public final class SwapModule<T extends Weapon> {
    /** 무기 인스턴스 */
    private final Swappable<T> weapon;
    /** 보조무기 인스턴스 */
    @NonNull
    @Getter
    private final T subweapon;
    /** 무기 교체시간 */
    private final Timespan swapDuration;

    /** 무기 전환 작업을 처리하는 태스크 */
    @Nullable
    private IntervalTask swapTask;
    /** 무기 전환 중 여부 */
    private boolean isSwapping = false;
    /** 보조무기 전환 상태 */
    @Getter
    private boolean isSwapped = false;

    /**
     * 2중 무기 모듈 인스턴스를 생성한다.
     *
     * @param weapon       대상 무기
     * @param subweapon    보조무기
     * @param swapDuration 무기 교체시간
     */
    public SwapModule(@NonNull Swappable<@NonNull T> weapon, @NonNull T subweapon, @NonNull Timespan swapDuration) {
        this.weapon = weapon;
        this.subweapon = subweapon;
        this.swapDuration = swapDuration;
    }

    /**
     * 이중 무기의 모드를 반대 무기로 교체한다.
     */
    public void swap() {
        if (isSwapping)
            return;

        isSwapping = true;
        weapon.onSwapStart(!isSwapped);

        long durationTicks = swapDuration.toTicks();

        swapTask = new IntervalTask(i -> {
            String message = MessageFormat.format("§c§l무기 교체 중... {0} §f[{1}초]",
                    StringFormUtil.getProgressBar(i, durationTicks, ChatColor.WHITE),
                    String.format("%.1f", Timespan.ofTicks(durationTicks - i).toSeconds()));

            weapon.getCombatUser().getUser().sendActionBar(message, Timespan.ofTicks(2));
        }, () -> {
            cancel();

            isSwapped = !isSwapped;
            weapon.getCombatUser().getUser().sendActionBar("§a§l무기 교체 완료", Timespan.ofTicks(6));
            weapon.onSwapFinished(isSwapped);
        }, 1, durationTicks);

        weapon.addTask(swapTask);
    }

    /**
     * 이중 무기의 전환을 취소한다.
     */
    public void cancel() {
        isSwapping = false;

        if (swapTask != null)
            swapTask.stop();
    }
}