package com.dace.dmgr.combat.entity.module;

import com.dace.dmgr.combat.entity.Healer;
import lombok.Getter;
import lombok.NonNull;

/**
 * 치유할 수 있는 엔티티의 모듈 클래스.
 *
 * <p>전투 시스템 엔티티가 {@link Healer}를 상속받는 클래스여야 한다.
 *
 * @see Healer
 */
@Getter
public final class HealerModule {
    /** 치유량 배수 기본값 */
    public static final double DEFAULT_VALUE = 1;

    /** 엔티티 객체 */
    @NonNull
    private final Healer combatEntity;
    /** 치유량 배수 값 */
    @NonNull
    private final AbilityStatus healMultiplierStatus;

    /**
     * 치유 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity   대상 엔티티
     * @param healMultiplier 치유량 배수 기본값. 0 이상의 값
     * @throws IllegalArgumentException 인자값이 유효하지 않으면 발생
     */
    public HealerModule(@NonNull Healer combatEntity, double healMultiplier) {
        if (healMultiplier < 0)
            throw new IllegalArgumentException("'healMultiplier'가 0 이상이어야 함");

        this.combatEntity = combatEntity;
        this.healMultiplierStatus = new AbilityStatus(healMultiplier);
    }

    /**
     * 치유 모듈 인스턴스를 생성한다.
     *
     * @param combatEntity 대상 엔티티
     */
    public HealerModule(@NonNull Healer combatEntity) {
        this(combatEntity, DEFAULT_VALUE);
    }
}
