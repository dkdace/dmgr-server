package com.dace.dmgr.combat.action.weapon;

import com.dace.dmgr.combat.action.weapon.module.AimModule;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

/**
 * 정조준이 가능한 무기의 인터페이스.
 */
public interface Aimable extends Weapon {
    /**
     * @return 정조준 모듈
     */
    @NonNull
    AimModule getAimModule();

    /**
     * 정조준 활성화 시 실행할 작업.
     */
    void onAimEnable();

    /**
     * 정조준 비활성화 시 실행할 작업.
     */
    void onAimDisable();

    /**
     * 조준 시 확대 레벨(화면이 확대되는 정도) 목록.
     *
     * @apiNote 각 단계는 임의로 지정된 값이며, 배율을 의미하지 않음
     */
    @AllArgsConstructor
    @Getter
    enum ZoomLevel {
        L1(1.2),
        L2(6),
        L3(-4.2),
        L4(-1.8),
        L5(-1.2),
        L6(-0.93),
        L7(-0.8),
        L8(-0.73),
        L9(-0.68),
        L10(-0.64);

        /** 실제 값 */
        private final double value;
    }
}