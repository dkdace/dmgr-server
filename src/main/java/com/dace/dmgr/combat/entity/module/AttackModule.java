package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Attacker;
import lombok.Getter;
import lombok.NonNull;

/**
 * 공격할 수 있는 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Attacker}를 상속받는 클래스여야 한다.
 *
 * @see Attacker
 */
@Getter
public final class AttackModule {
    /** 공격력 배수 기본값 */
    private static final double DEFAULT_VALUE = 1;

    /** 엔티티 객체 */
    @NonNull
    private final Attacker combatEntity;
    /** 공격력 배수 값 */
    @NonNull
    private final AbilityStatus damageMultiplierStatus;

    /**
     * 공격 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity     대상 엔티티
     * @param damageMultiplier 공격력 배수 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public AttackModule(@NonNull Attacker combatEntity, double damageMultiplier) {
        if (damageMultiplier < 0)
            throw new IllegalArgumentException("'damageMultiplier'가 0 이상이어야 함");

        this.combatEntity = combatEntity;
        this.damageMultiplierStatus = new AbilityStatus(damageMultiplier);
    }

    /**
     * 공격 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public AttackModule(@NonNull Attacker combatEntity) {
        this(combatEntity, DEFAULT_VALUE);
    }
}
