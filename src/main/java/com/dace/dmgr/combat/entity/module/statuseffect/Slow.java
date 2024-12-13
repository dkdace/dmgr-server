package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.util.ParticleEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Material;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 둔화 상태 효과를 처리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Slow implements StatusEffect {
    /** 틱 입자 효과 */
    private static final ParticleEffect TICK_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.WOOL, 12).count(3)
                    .horizontalSpread(0, 0, 0.5)
                    .build());

    /** 수정자 ID */
    private final String modifierId;
    /** 이동 속도 감소량 */
    private final double decrement;

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.SLOW;
    }

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(modifierId, -decrement);
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity.getDamageModule().isLiving())
            TICK_PARTICLE.play(combatEntity.getEntity().getLocation().add(0, 0.5, 0), combatEntity.getEntity().getWidth());
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(modifierId);
    }
}
