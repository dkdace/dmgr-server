package com.dace.dmgr.combat.action.weapon;

import com.comphenix.packetwrapper.WrapperPlayServerAbilities;
import com.dace.dmgr.system.task.ActionTaskTimer;
import com.dace.dmgr.system.task.TaskManager;
import lombok.Getter;

/**
 * 정조준이 가능한 무기의 인터페이스.
 */
public interface Aimable extends Weapon {
    /**
     * @return 정조준 상태
     */
    boolean isAiming();

    /**
     * @param aiming 정조준 상태
     */
    void setAiming(boolean aiming);

    /**
     * @return 확대 레벨
     */
    ZoomLevel getZoomLevel();

    /**
     * 플레이어에게 화면 확대 효과를 재생한다.
     *
     * @param value 값
     */
    default void playZoomEffect(float value) {
        WrapperPlayServerAbilities packet = new WrapperPlayServerAbilities();

        packet.setWalkingSpeed(getCombatUser().getEntity().getWalkSpeed() * value);

        packet.sendPacket(getCombatUser().getEntity());
    }

    /**
     * 무기 정조준을 활성화 또는 비활성화한다.
     */
    default void toggleAim() {
        if (this instanceof Swappable && ((Swappable<?>) this).getSwapState() == Swappable.SwapState.SWAPPING)
            return;

        setAiming(!isAiming());

        if (isAiming()) {
            onAimEnable();

            TaskManager.addTask(this, new ActionTaskTimer(getCombatUser(), 1) {
                @Override
                public boolean onTickAction(int i) {
                    if (!isAiming())
                        return false;
                    if (Aimable.this instanceof Reloadable && ((Reloadable) Aimable.this).isReloading())
                        return false;

                    playZoomEffect(getZoomLevel().getValue());

                    return true;
                }

                @Override
                public void onEnd(boolean cancelled) {
                    setAiming(false);
                    onAimDisable();
                }
            });
        }
    }

    /**
     * {@link Aimable#toggleAim()}에서 정조준 활성화 시 실행할 작업.
     */
    void onAimEnable();

    /**
     * {@link Aimable#toggleAim()}에서 정조준 비활성화 시 실행할 작업.
     */
    void onAimDisable();

    /**
     * 조준 시 확대 레벨(화면이 확대되는 정도) 목록.
     */
    @Getter
    enum ZoomLevel {
        L1(1.2F),
        L2(6F),
        L3(-4.2F),
        L4(-1.8F),
        L5(-1.2F),
        L6(-0.93F),
        L7(-0.8F),
        L8(-0.73F),
        L9(-0.68F),
        L10(-0.64F);

        /** 실제 값 */
        private final float value;

        ZoomLevel(float value) {
            this.value = value;
        }
    }
}