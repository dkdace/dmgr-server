package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.DamageType;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.effect.ParticleEffect;
import lombok.NonNull;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 독 상태 효과를 처리하는 클래스.
 */
public class Poison extends StatusEffect {
    /** 틱 입자 효과 */
    private static final ParticleEffect TICK_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(Particle.DAMAGE_INDICATOR)
                    .horizontalSpread(0, 0, 0.25)
                    .verticalSpread(1, 0, 0.25)
                    .speed(0.3).build());

    /** 초당 피해량 */
    private final double damagePerSecond;
    /** 궁극기 제공 여부 */
    private final boolean isUlt;

    /**
     * 독 상태 효과 인스턴스를 생성한다.
     *
     * @param damagePerSecond 초당 피해량
     * @param isUlt           궁극기 제공 여부
     */
    protected Poison(double damagePerSecond, boolean isUlt) {
        super(StatusEffectType.POISON, false);

        this.damagePerSecond = damagePerSecond;
        this.isUlt = isUlt;
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (!combatEntity.isCreature())
            return;

        if (combatEntity.getEntity() instanceof LivingEntity)
            ((LivingEntity) combatEntity.getEntity()).addPotionEffect(
                    new PotionEffect(PotionEffectType.POISON, 4, 0, false, false), true);

        if (i % 2 == 0)
            TICK_PARTICLE.play(combatEntity.getCenterLocation(), combatEntity.getWidth(), combatEntity.getHeight());

        if (i % 10 == 0 && provider instanceof Attacker)
            combatEntity.getDamageModule().damage((Attacker) provider, damagePerSecond * 10 / 20.0, DamageType.IGNORE_DEFENSE, null,
                    false, isUlt);
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
