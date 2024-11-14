package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestrictions;
import com.dace.dmgr.combat.entity.Damageable;
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
     * 상태 효과가 이로운 효과인지 확인한다.
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
    void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider);

    /**
     * 상태 효과 적용 중 매 틱마다 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     * @param i            인덱스
     */
    void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i);

    /**
     * 상태 효과가 끝났을 때 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     */
    void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider);

    /**
     * 상태 효과가 있을 때 제한할 행동들을 반환한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 제한할 행동 플래그의 비트 합
     * @see CombatRestrictions
     */
    default long getCombatRestrictions(@NonNull Damageable combatEntity) {
        return CombatRestrictions.NONE;
    }
}
