package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Living;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.util.ParticleUtil;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 속도 증가 상태 효과를 처리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Speed implements StatusEffect {
    /** 수정자 ID */
    private final String modifierId;
    /** 이동 속도 증가량 */
    private final double increment;

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.SPEED;
    }

    @Override
    public final boolean isPositive() {
        return true;
    }

    @Override
    @MustBeInvokedByOverriders
    public void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(modifierId, increment);
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof Living)
            ParticleUtil.playRGB(ParticleUtil.ColoredParticle.SPELL_MOB_AMBIENT, combatEntity.getEntity().getLocation().add(0, 0.1, 0),
                    3, combatEntity.getEntity().getWidth() / 4, 0, combatEntity.getEntity().getWidth() / 4,
                    200, 255, 255);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(modifierId);
    }
}
