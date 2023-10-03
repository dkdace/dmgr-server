package com.dace.dmgr.combat.action.weapon;

/**
 * 정조준이 가능한 무기의 인터페이스.
 */
public interface Aimable {
    /**
     * @return 정조준 상태
     */
    boolean isAiming();

    /**
     * @return 확대 레벨
     */
    AimModule.ZoomLevel getZoomLevel();

    /**
     * 정조준 이벤트를 호출한다.
     */
    void aim();
}