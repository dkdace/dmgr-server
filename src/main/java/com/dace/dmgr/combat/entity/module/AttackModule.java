package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Attacker;
import lombok.Getter;
import lombok.NonNull;

/**
 * 공격할 수 있는 엔티티의 모듈 클래스.
 *
 * @see Attacker
 */
@Getter
public final class AttackModule {
    /** 공격력 배수 기본값 */
    private static final double DEFAULT_VALUE = 1;
    /** 공격력 배수 값 */
    @NonNull
    private final AbilityStatus damageMultiplierStatus;

    /**
     * 공격 모듈 인스턴스를 생성한다.
     */
    public AttackModule() {
        this.damageMultiplierStatus = new AbilityStatus(DEFAULT_VALUE);
    }
}
