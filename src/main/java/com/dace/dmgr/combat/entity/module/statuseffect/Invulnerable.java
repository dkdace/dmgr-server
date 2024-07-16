package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 무적 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Invulnerable implements StatusEffect {
    @Getter
    static final Invulnerable instance = new Invulnerable();

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
    public void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
        // 미사용
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
