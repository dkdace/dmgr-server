package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Healer;
import lombok.Getter;
import lombok.NonNull;

/**
 * 치유할 수 있는 엔티티의 모듈 클래스.
 *
 * @see Healer
 */
@Getter
public final class HealerModule {
    /** 치유량 배수 기본값 */
    private static final double DEFAULT_VALUE = 1;
    /** 치유량 배수 값 */
    @NonNull
    private final AbilityStatus healMultiplierStatus;

    /**
     * 치유 모듈 인스턴스를 생성한다.
     */
    public HealerModule() {
        this.healMultiplierStatus = new AbilityStatus(DEFAULT_VALUE);
    }
}
