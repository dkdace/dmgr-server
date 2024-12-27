package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.*;
import com.dace.dmgr.Timespan;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 속박 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Snare implements StatusEffect {
    @Getter
    private static final Snare instance = new Snare();

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
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier("Snare", -100);
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l속박당함!", "", Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier("Snare");
    }

    @Override
    public long getCombatRestrictions(@NonNull Damageable combatEntity) {
        return CombatRestrictions.DEFAULT_MOVE | CombatRestrictions.ACTION_MOVE;
    }
}
