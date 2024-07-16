package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.util.ParticleUtil;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * 독 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Poison implements StatusEffect {
    @Getter
    static final Poison instance = new Poison();

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
    public void onTick(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity.getEntity() instanceof LivingEntity)
            ((LivingEntity) combatEntity.getEntity()).addPotionEffect(new PotionEffect(PotionEffectType.POISON,
                    4, 0, false, false), true);

        if (i % 2 == 0)
            ParticleUtil.play(Particle.DAMAGE_INDICATOR, combatEntity.getEntity().getLocation().add(0, combatEntity.getEntity().getHeight() / 2, 0),
                    1, 0.1, 0.3, 0.1, 0.3);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }
}
