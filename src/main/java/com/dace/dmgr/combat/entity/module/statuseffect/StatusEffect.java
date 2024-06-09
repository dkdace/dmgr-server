package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.NonNull;

/**
 * 상태 효과를 처리하는 인터페이스.
 */
public interface StatusEffect {
    /**
     * 상태 효과의 종류를 반환한다.
     *
     * @return 상태 효과 종류
     */
    @NonNull
    StatusEffectType getStatusEffectType();

    /**
     * 상태 효과가 이로운 효과인 지 확인한다.
     *
     * @return {@code true} 반환 시 이로운 효과, {@code false} 반환 시 해로운 효과
     */
    boolean isPositive();

    /**
     * 상태 효과 적용 시 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     */
    void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider);

    /**
     * 상태 효과 적용 중 매 틱마다 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     * @param i            인덱스
     */
    void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i);

    /**
     * 상태 효과가 끝났을 때 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     */
    void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider);
}
