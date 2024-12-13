package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.ParticleEffect;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 독 상태 효과를 처리하는 클래스.
 */
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Poison implements StatusEffect {
    /** 틱 입자 효과 */
    private static final ParticleEffect TICK_PARTICLE = new ParticleEffect(
            ParticleEffect.NormalParticleInfo.builder(Particle.DAMAGE_INDICATOR)
                    .horizontalSpread(0, 0, 0.25)
                    .verticalSpread(1, 0, 0.25)
                    .speed(0.3).build());

    /** 초당 피해량 */
    private final int damagePerSecond;

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.POISON;
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
        if (combatEntity.getDamageModule().isLiving()) {
            if (combatEntity.getEntity() instanceof LivingEntity)
                ((LivingEntity) combatEntity.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                        4, 0, false, false), true);

            if (i % 2 == 0)
                TICK_PARTICLE.play(combatEntity.getCenterLocation(), combatEntity.getEntity().getWidth(), combatEntity.getEntity().getHeight());

            if (i % 10 == 0 && provider instanceof Attacker)
                combatEntity.getDamageModule().damage((Attacker) provider, damagePerSecond * 10 / 20.0,
                        DamageType.IGNORE_DEFENSE, null, false, true);
        }
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
