package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.combat.action.weapon.Weapon;
import com.dace.dmgr.util.StringFormUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.bukkit.ChatColor;

import java.text.MessageFormat;

/**
 * 무기의 2중 무기 모듈 클래스.
 *
 * <p>무기가 {@link Swappable}을 상속받는 클래스여야 한다.</p>
 *
 * @param <T> {@link Weapon}을 상속받는 보조무기
 * @see Swappable
 */
public final class SwapModule<T extends Weapon> {
    /** 무기 객체 */
    private final Swappable<T> weapon;
    /** 보조무기 객체 */
    @NonNull
    @Getter
    private final T subweapon;
    /** 무기 교체시간 (tick) */
    private final long swapDuration;

    /** 무기 전환 중 여부 */
    @Getter
    @Setter
    private boolean isSwapping = false;
    /** 무기 전환 상태 */
    @NonNull
    @Getter
    private Swappable.SwapState swapState = Swappable.SwapState.PRIMARY;

    /**
     * 2중 무기 모듈 인스턴스를 생성한다.
     *
     * @param weapon       대상 무기
     * @param subweapon    보조무기 객체
     * @param swapDuration 무기 교체시간 (tick). 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public SwapModule(@NonNull Swappable<@NonNull T> weapon, @NonNull T subweapon, long swapDuration) {
        if (swapDuration < 0)
            throw new IllegalArgumentException("'swapDuration'이 0 이상이어야 함");

        this.weapon = weapon;
        this.subweapon = subweapon;
        this.swapDuration = swapDuration;
    }

    /**
     * 이중 무기의 상태를 변경한다.
     *
     * @param targetState 변경할 상태
     */
    private void swapTo(@NonNull Swappable.SwapState targetState) {
        if (getSwapState() == targetState || isSwapping)
            return;

        isSwapping = true;
        weapon.onSwapStart(targetState);

        TaskUtil.addTask(weapon, new IntervalTask(i -> {
            if (!isSwapping)
                return false;

            String time = String.format("%.1f", (swapDuration - i) / 20.0);
            weapon.getCombatUser().getUser().sendActionBar(MessageFormat.format("§c§l무기 교체 중... {0} §f[{1}초]",
                    StringFormUtil.getProgressBar(i, swapDuration, ChatColor.WHITE), time), 2);

            return true;
        }, isCancelled -> {
            if (isCancelled)
                return;

            swapState = targetState;
            isSwapping = false;
            weapon.getCombatUser().getUser().sendActionBar("§a§l무기 교체 완료", 6);
            weapon.onSwapFinished(targetState);
        }, 1, swapDuration));
    }

    /**
     * 이중 무기의 모드를 반대 무기로 교체한다.
     */
    public void swap() {
        if (swapState == Swappable.SwapState.PRIMARY)
            swapTo(Swappable.SwapState.SECONDARY);
        else if (swapState == Swappable.SwapState.SECONDARY)
            swapTo(Swappable.SwapState.PRIMARY);
    }
}