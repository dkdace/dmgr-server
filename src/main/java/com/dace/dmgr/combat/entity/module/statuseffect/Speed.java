package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Movable;
import com.dace.dmgr.combat.entity.module.AbilityStatus;
import com.dace.dmgr.effect.ParticleEffect;
import com.dace.dmgr.effect.PlayableEffect;
import lombok.NonNull;
import org.bukkit.Color;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 속도 증가 상태 효과를 처리하는 클래스.
 */
public class Speed implements StatusEffect {
    /** 틱 입자 효과 */
    private static final PlayableEffect.Function<CombatEntity> TICK_PARTICLE = combatEntity ->
            ParticleEffect.Colored.builder(ParticleEffect.Colored.ParticleType.SPELL_MOB_AMBIENT, Color.fromRGB(200, 255, 255))
                    .count(3).horizontalSpread(combatEntity.getWidth() * 0.25).verticalSpread(combatEntity.getHeight() * 0.25).build();
    /** 수정자 */
    private final AbilityStatus.Modifier modifier;

    /**
     * 속도 증가 상태 효과 인스턴스를 생성한다.
     *
     * @param increment 이동 속도 증가량
     */
    public Speed(double increment) {
        this.modifier = new AbilityStatus.Modifier(increment);
    }

    /**
     * @return 이동 속도 증가량
     */
    protected final double getIncrement() {
        return modifier.getIncrement();
    }

    /**
     * 이동 속도 증가량을 설정한다.
     *
     * @param increment 이동 속도 증가량
     */
    protected final void setIncrement(double increment) {
        modifier.setIncrement(increment);
    }

    @Override
    public final boolean isPositive() {
        return true;
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
            TICK_PARTICLE.apply(combatEntity).play(combatEntity.getLocation().add(0, 0.1, 0));
    }

    @Override
    @MustBeInvokedByOverriders
    public void onEnd(@NonNull Damageable combatEntity) {
        if (combatEntity instanceof Movable)
            ((Movable) combatEntity).getMoveModule().getSpeedStatus().removeModifier(modifier);
    }
}
