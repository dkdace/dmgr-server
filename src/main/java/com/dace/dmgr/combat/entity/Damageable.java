package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.DamageType;
import com.dace.dmgr.combat.entity.module.DamageModule;

/**
 * 생명력 수치를 조정하고 피해를 입을 수 있는 엔티티의 인터페이스.
 */
public interface Damageable extends CombatEntity {
    /**
     * @return 피해 모듈
     */
    DamageModule getDamageModule();

    /**
     * 엔티티가 피해를 받을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 피해 가능 여부
     */
    default boolean canTakeDamage() {
        return true;
    }

    /**
     * 엔티티가 죽을 수 있는 지 확인한다.
     *
     * <p>기본값은 {@code true}이며, 오버라이딩하여 재설정할 수 있다.</p>
     *
     * @return 죽을 수 있으면 {@code true} 반환
     */
    default boolean canDie() {
        return true;
    }

    /**
     * 엔티티가 피해를 입었을 때 실행될 작업.
     *
     * @param attacker   공격자
     * @param damage     피해량
     * @param damageType 타입
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     * @see Attacker#onAttack(Damageable, int, DamageType, boolean, boolean)
     */
    void onDamage(Attacker attacker, int damage, DamageType damageType, boolean isCrit, boolean isUlt);

    /**
     * 엔티티가 죽었을 때 실행될 작업.
     *
     * @param attacker 공격자
     * @see Attacker#onKill(CombatEntity)
     */
    void onDeath(Attacker attacker);
}
