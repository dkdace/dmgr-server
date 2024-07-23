package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.Attacker;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.Living;
import com.dace.dmgr.combat.interaction.DamageType;
import com.dace.dmgr.util.ParticleUtil;
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
    /** 초당 피해량 */
    private final int dps;

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
    public void onStart(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof Living) {
            if (combatEntity.getEntity() instanceof LivingEntity)
                ((LivingEntity) combatEntity.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                        4, 0, false, false), true);

            if (i % 2 == 0)
                ParticleUtil.play(Particle.DAMAGE_INDICATOR, combatEntity.getCenterLocation(),
                        1, combatEntity.getEntity().getWidth() / 2, combatEntity.getEntity().getHeight() / 2,
                        combatEntity.getEntity().getWidth() / 2, 0.3);

            if (i % 10 == 0 && combatEntity instanceof Damageable && provider instanceof Attacker)
                ((Damageable) combatEntity).getDamageModule().damage((Attacker) provider, dps * 10 / 20,
                        DamageType.IGNORE_DEFENSE, null, false, true);
        }
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
