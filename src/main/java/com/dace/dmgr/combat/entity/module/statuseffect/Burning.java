package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.NamedSound;
import com.dace.dmgr.util.SoundUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 화염 상태 효과를 처리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Burning implements StatusEffect {
    /** 초당 피해량 */
    private final int dps;
    /** 궁극기 충전 여부 */
    private final boolean isUlt;

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.BURNING;
    }

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        combatEntity.getEntity().setFireTicks(4);

        if (i % 10 == 0 && provider instanceof Attacker && combatEntity.getDamageModule().damage((Attacker) provider, dps * 10 / 20,
                DamageType.NORMAL, null, false, isUlt))
            SoundUtil.playNamedSound(NamedSound.COMBAT_DAMAGE_BURNING, combatEntity.getEntity().getLocation());
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
