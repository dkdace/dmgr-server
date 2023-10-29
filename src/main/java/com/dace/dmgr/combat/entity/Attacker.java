package com.dace.dmgr.combat.entity;

import com.dace.dmgr.combat.DamageType;

/**
 * 다른 엔티티를 공격할 수 있는 엔티티의 인터페이스.
 */
public interface Attacker extends CombatEntity {
    /**
     * 엔티티가 다른 엔티티를 공격했을 때 실행될 작업.
     *
     * @param victim     피격자
     * @param damage     피해량
     * @param damageType 피해 타입
     * @param isCrit     치명타 여부
     * @param isUlt      궁극기 충전 여부
     * @see Damageable#onDamage(Attacker, int, DamageType, boolean, boolean)
     */
    void onAttack(Damageable victim, int damage, DamageType damageType, boolean isCrit, boolean isUlt);

    /**
     * 엔티티가 다른 엔티티를 죽였을 때 실행될 작업.
     *
     * @param victim 피격자
     * @see Damageable#onDeath(Attacker)
     */
    void onKill(CombatEntity victim);

    /**
     * 엔티티가 기본 공격을 했을 때 실행될 작업.
     *
     * @param victim 피격자
     */
    default void onDefaultAttack(Damageable victim) {
    }
}
