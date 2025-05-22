package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * 무적 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invulnerable implements StatusEffect {
    @Getter
    private static final Invulnerable instance = new Invulnerable();

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.INVULNERABLE;
    }

    @Override
    public final boolean isPositive() {
        return true;
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, long i) {
        // 미사용
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    @NonNull
    public final Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.DAMAGED);
    }
}
