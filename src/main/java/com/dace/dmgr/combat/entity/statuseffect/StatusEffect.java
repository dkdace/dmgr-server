package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;

/**
 * 상태 효과를 처리하는 인터페이스.
 */
public interface StatusEffect {
    /**
     * @return 상태 효과의 종류
     */
    StatusEffectType getStatusEffectType();

    /**
     * 상태 효과 적용 시 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     */
    void onStart(CombatEntity<?> combatEntity);

    /**
     * 상태 효과 적용 중 매 틱마다 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param i            인덱스
     */
    void onTick(CombatEntity<?> combatEntity, int i);

    /**
     * 상태 효과가 끝났을 때 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     */
    void onEnd(CombatEntity<?> combatEntity);
}
