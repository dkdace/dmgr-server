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
 * 기절 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stun implements StatusEffect {
    @Getter
    static final Stun instance = new Stun();

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.STUN;
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

            if (provider instanceof CombatUser)
                ((CombatUser) combatEntity).cancelAction((CombatUser) provider);
        }
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l기절함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    public long getCombatRestrictions(@NonNull Damageable combatEntity) {
        return CombatRestrictions.DEFAULT_MOVE | CombatRestrictions.USE_ACTION;
    }
}
