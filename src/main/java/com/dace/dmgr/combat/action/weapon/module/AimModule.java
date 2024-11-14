package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

/**
 * 무기의 정조준 모듈 클래스.
 *
 * <p>무기가 {@link Aimable}을 상속받는 클래스여야 한다.</p>
 *
 * @see Aimable
 */
@RequiredArgsConstructor
public final class AimModule {
    /** 무기 객체 */
    @NonNull
    private final Aimable weapon;
    /** 확대 레벨 */
    @NonNull
    private final Aimable.ZoomLevel zoomLevel;

    /** 정조준 상태 */
    @Getter
    private boolean isAiming = false;

    /**
     * 무기 정조준을 활성화 또는 비활성화한다.
     */
    public void toggleAim() {
        isAiming = !isAiming;

        if (isAiming) {
            weapon.onAimEnable();

            TaskUtil.addTask(weapon, new IntervalTask(i -> {
                if (!isAiming)
                    return false;

                weapon.getCombatUser().setFovValue(zoomLevel.getValue());

                return true;
            }, isCancelled -> {
                isAiming = false;
                weapon.getCombatUser().setFovValue(0);
                weapon.onAimDisable();
            }, 1));
        }
    }

    /**
     * 무기의 정조준을 취소한다.
     */
    public void cancel() {
        isAiming = false;
    }
}
