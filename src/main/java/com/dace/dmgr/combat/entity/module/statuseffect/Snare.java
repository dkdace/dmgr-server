package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Movable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 속박 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Snare implements StatusEffect {
    @Getter
    static final Snare instance = new Snare();

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.SNARE;
    }

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier("Snare", -100);
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l속박당함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier("Snare");
    }
}
