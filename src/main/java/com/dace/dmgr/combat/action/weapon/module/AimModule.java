package com.dace.dmgr.combat.action.weapon.module;

import com.comphenix.packetwrapper.WrapperPlayServerAbilities;
import com.dace.dmgr.combat.action.weapon.Aimable;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.action.weapon.Swappable;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
    @Setter
    private boolean isAiming = false;

    /**
     * 플레이어에게 화면 확대 효과를 재생한다.
     *
     * @param value 값
     */
    private void playZoomEffect(double value) {
        WrapperPlayServerAbilities packet = new WrapperPlayServerAbilities();

        packet.setWalkingSpeed((float) (weapon.getCombatUser().getEntity().getWalkSpeed() * value));

        packet.sendPacket(weapon.getCombatUser().getEntity());
    }

    /**
     * 무기 정조준을 활성화 또는 비활성화한다.
     */
    public void toggleAim() {
        if (weapon instanceof Swappable && ((Swappable<?>) weapon).getSwapModule().getSwapState() == Swappable.SwapState.SWAPPING)
            return;

        isAiming = !isAiming;

        if (isAiming) {
            weapon.onAimEnable();

            TaskUtil.addTask(weapon.getTaskRunner(), new IntervalTask(i -> {
                if (!isAiming)
                    return false;
                if (weapon instanceof Reloadable && ((Reloadable) weapon).getReloadModule().isReloading())
                    return false;

                playZoomEffect(zoomLevel.getValue());

                return true;
            }, isCancelled -> {
                isAiming = false;
                weapon.onAimDisable();
            }, 1));
        }
    }
}
