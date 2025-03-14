package com.dace.dmgr.combat.action.weapon.module;

import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Nullable;

import java.util.function.LongConsumer;

/**
 * 무기의 정조준 모듈 클래스.
 *
 * @see Aimable
 */
@RequiredArgsConstructor
public final class AimModule {
    /** 무기 인스턴스 */
    @NonNull
    private final Aimable weapon;
    /** 확대 레벨 */
    @NonNull
    private final Aimable.ZoomLevel zoomLevel;

    /** 틱 작업을 처리하는 태스크 */
    @Nullable
    private IntervalTask onTickTask;
    /** 정조준 상태 */
    @Getter
    private boolean isAiming = false;

    /**
     * 무기 정조준을 활성화 또는 비활성화한다.
     */
    public void toggleAim() {
        if (isAiming) {
            cancel();
            return;
        }

        isAiming = true;

        weapon.onAimEnable();

        onTickTask = new IntervalTask((LongConsumer) i -> weapon.getCombatUser().setFovValue(zoomLevel.getValue()), 1);
        weapon.addTask(onTickTask);
    }

    /**
     * 무기의 정조준을 취소한다.
     */
    public void cancel() {
        if (!isAiming)
            return;

        isAiming = false;

        if (onTickTask != null)
            onTickTask.stop();

        weapon.getCombatUser().setFovValue(0);
        weapon.onAimDisable();
    }
}
