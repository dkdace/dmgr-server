package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.weapon.module.AimModule;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 정조준이 가능한 무기의 인터페이스.
 */
public interface Aimable extends Weapon {
    /**
     * @return 정조준 모듈
     */
    AimModule getAimModule();

    /**
     * 정조준 활성화 시 실행할 작업.
     */
    default void onAimEnable() {
    }

    /**
     * 정조준 비활성화 시 실행할 작업.
     */
    default void onAimDisable() {
    }

    /**
     * 조준 시 확대 레벨(화면이 확대되는 정도) 목록.
     */
    @AllArgsConstructor
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
    }
}