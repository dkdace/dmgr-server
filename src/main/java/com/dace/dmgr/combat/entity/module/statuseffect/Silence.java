package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 침묵 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Silence implements StatusEffect {
    @Getter
    static final Silence instance = new Silence();

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.SILENCE;
    }

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).cancelAction();
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, long i) {
        if (combatEntity instanceof CombatUser) {
            ((CombatUser) combatEntity).getUser().sendTitle("§5§l침묵당함!", "", 0, 2, 10);
            ((CombatUser) combatEntity).getEntity().stopSound("");
        }
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity) {
        // 미사용
    }
}
