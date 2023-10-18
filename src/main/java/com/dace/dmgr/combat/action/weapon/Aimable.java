package com.dace.dmgr.combat.action.weapon;

import lombok.Getter;

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
    ZoomLevel getZoomLevel();

    /**
     * 정조준 이벤트를 호출한다.
     */
    void aim();

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