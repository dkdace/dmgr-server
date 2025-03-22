package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.effect.ParticleEffect;
import lombok.NonNull;
import org.bukkit.Color;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 속도 증가 상태 효과를 처리하는 클래스.
 */
public class Speed extends StatusEffect {
    /** 틱 입자 효과 */
    private static final ParticleEffect TICK_PARTICLE = new ParticleEffect(
            ParticleEffect.ColoredParticleInfo.builder(ParticleEffect.ColoredParticleInfo.ParticleType.SPELL_MOB_AMBIENT,
                            Color.fromRGB(200, 255, 255)).count(3)
                    .horizontalSpread(0, 0, 0.25)
                    .verticalSpread(1, 0, 0.25)
                    .build());
    /** 수정자 */
    private final AbilityStatus.Modifier modifier;

    /**
     * 속도 증가 상태 효과 인스턴스를 생성한다.
     *
     * @param increment 이동 속도 증가량
     */
    protected Speed(double increment) {
        super(StatusEffectType.SPEED, true);
        this.modifier = new AbilityStatus.Modifier(increment);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().addModifier(modifier);
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity.isCreature())
            TICK_PARTICLE.play(combatEntity.getLocation().add(0, 0.1, 0), combatEntity.getWidth(), combatEntity.getHeight());
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(modifier);
    }
}
