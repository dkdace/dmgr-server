package com.dace.dmgr.combat.entity;

/**
 * 다른 엔티티를 치유할 수 있는 엔티티의 인터페이스.
 */
public interface Healer extends CombatEntity {
    /**
     * 엔티티가 다른 엔티티를 치유했을 때 실행될 작업.
     *
     * @param target 수급자
     * @param amount 치유량
     * @param isUlt  궁극기 충전 여부
     * @see Healable#onTakeHeal(CombatEntity, int, boolean)
     */
    void onGiveHeal(Healable target, int amount, boolean isUlt);
}
