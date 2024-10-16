package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.apache.commons.lang3.Validate;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

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
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof CombatUser) {
            Validate.notNull(((CombatUser) combatEntity).getCharacterType());

            ((CombatUser) combatEntity).getUser().sendTitle("§5§l침묵당함!", "", 0, 5, 10);

            if (provider instanceof CombatUser)
                ((CombatUser) combatEntity).cancelSkill((CombatUser) provider);
        }
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().stopSound("");
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    public long getCombatRestrictions(@NonNull Damageable combatEntity) {
        return CombatRestrictions.USE_SKILL | CombatRestrictions.HEAR;
    }
}
