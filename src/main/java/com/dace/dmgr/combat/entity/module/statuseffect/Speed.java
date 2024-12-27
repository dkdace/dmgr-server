package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.effect.ParticleEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 속도 증가 상태 효과를 처리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Speed implements StatusEffect {
    /** 틱 입자 효과 */
    private static final ParticleEffect TICK_PARTICLE = new ParticleEffect(
            ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB_AMBIENT, 200, 255, 255)
                    .count(3)
                    .horizontalSpread(0, 0, 0.25)
                    .verticalSpread(1, 0, 0.25)
                    .build());

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
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(modifierId, increment);
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity.getDamageModule().isLiving())
            TICK_PARTICLE.play(combatEntity.getEntity().getLocation().add(0, 0.1, 0), combatEntity.getEntity().getWidth(),
                    combatEntity.getEntity().getHeight());
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(modifierId);
    }
}
