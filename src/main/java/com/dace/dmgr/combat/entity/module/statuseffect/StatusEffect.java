package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * 상태 효과를 처리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
public abstract class StatusEffect {
    /** 상태 효과의 유형 */
    @NonNull
    private final StatusEffectType statusEffectType;
    /** 이로운 효과 여부 */
    private final boolean isPositive;

    /**
     * 상태 효과 처리 인스턴스를 생성한다.
     *
     * @param isPositive 이로운 효과 여부. {@code true}로 지정 시 이로운 효과, {@code false}로 지정 시 해로운 효과
     */
    protected StatusEffect(boolean isPositive) {
        this(StatusEffectType.NONE, isPositive);
    }

    /**
     * 상태 효과 적용 시 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     */
    public abstract void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider);

    /**
     * 상태 효과 적용 중 매 틱마다 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     * @param i            인덱스
     */
    public abstract void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i);

    /**
     * 상태 효과가 끝났을 때 실행할 작업.
     *
     * @param combatEntity 대상 엔티티
     * @param provider     제공자
     */
    public abstract void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider);

    /**
     * 상태 효과가 있을 때 제한할 행동들을 반환한다.
     *
     * @param combatEntity 대상 엔티티
     * @return 제한할 행동 목록
     * @see CombatRestriction
     */
    @NonNull
    public Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.NONE);
    }
}
