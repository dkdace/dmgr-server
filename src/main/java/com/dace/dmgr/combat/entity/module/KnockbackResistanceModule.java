package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.AbilityStatus;
import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.Getter;
import lombok.NonNull;

/**
 * 엔티티의 넉백 저항 모듈 클래스.
 */
@Getter
public final class KnockbackResistanceModule {
    /** 기본값 */
    private static final double DEFAULT_VALUE = 0;
    /** 엔티티 객체 */
    @NonNull
    private final CombatEntity combatEntity;
    /** 넉백 저항 배수 값 */
    @NonNull
    private final AbilityStatus resistanceMultiplierStatus;

    /**
     * 넉백 저항 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     * @param resistance   넉백 저항 기본값
     */
    public KnockbackResistanceModule(@NonNull CombatEntity combatEntity, double resistance) {
        this.combatEntity = combatEntity;
        this.resistanceMultiplierStatus = new AbilityStatus(resistance);
    }

    /**
     * 넉백 저항 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public KnockbackResistanceModule(@NonNull CombatEntity combatEntity) {
        this(combatEntity, DEFAULT_VALUE);
    }
}
