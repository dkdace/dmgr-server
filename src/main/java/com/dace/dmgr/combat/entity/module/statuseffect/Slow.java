package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.effect.ParticleEffect;
import lombok.NonNull;
import org.bukkit.Material;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 둔화 상태 효과를 처리하는 클래스.
 */
public class Slow extends StatusEffect {
    /** 틱 입자 효과 */
    private static final ParticleEffect TICK_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(ParticleEffect.BlockParticleType.FALLING_DUST, Material.WOOL, 12).count(3)
                    .horizontalSpread(0, 0, 0.5)
                    .build());
    /** 수정자 */
    private final AbilityStatus.Modifier modifier;

    /**
     * 둔화 상태 효과 인스턴스를 생성한다.
     *
     * @param decrement 이동 속도 감소량
     */
    public Slow(double decrement) {
        super(StatusEffectType.SLOW, false);
        this.modifier = new AbilityStatus.Modifier(-decrement);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(modifier);
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, long i) {
        if (combatEntity.isCreature())
            TICK_PARTICLE.play(combatEntity.getLocation().add(0, 0.5, 0), combatEntity.getWidth());
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull Damageable combatEntity) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(modifier);
    }
}
