package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * 상태 효과를 처리하는 인터페이스.
 */
public interface StatusEffect {
    /**
     * @return 상태 효과의 유형
     * @implSpec {@link StatusEffectType#NONE}
     */
    @NonNull
    default StatusEffectType getStatusEffectType() {
        return StatusEffectType.NONE;
    }

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
     */
    void onStart(@NonNull Damageable combatEntity);

    /**
     * 상태 효과 적용 중 매 틱마다 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param i            인덱스
     */
    void onTick(@NonNull Damageable combatEntity, long i);

    /**
     * 상태 효과가 끝났을 때 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     */
    void onEnd(@NonNull Damageable combatEntity);

    /**
     * 상태 효과가 있을 때 제한할 행동들을 반환한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 제한할 행동 목록
     * @implSpec {@link CombatRestriction#NONE}
     * @see CombatRestriction
     */
    @NonNull
    default Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.NONE);
    }
}
