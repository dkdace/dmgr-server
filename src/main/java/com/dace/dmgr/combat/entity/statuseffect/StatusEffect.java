package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.NonNull;

/**
 * 상태 효과를 처리하는 인터페이스.
 */
public interface StatusEffect {
    /**
     * @return 상태 효과 이름
     */
    @NonNull
    String getName();

    /**
     * 상태 효과 적용 시 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     */
    void onStart(@NonNull CombatEntity combatEntity);

    /**
     * 상태 효과 적용 중 매 틱마다 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param i            인덱스
     */
    void onTick(@NonNull CombatEntity combatEntity, long i);

    /**
     * 상태 효과가 끝났을 때 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     */
    void onEnd(@NonNull CombatEntity combatEntity);
}
