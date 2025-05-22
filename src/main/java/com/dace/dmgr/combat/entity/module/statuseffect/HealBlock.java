package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.EnumSet;
import java.util.Set;

/**
 * 회복 차단 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HealBlock implements StatusEffect {
    @Getter
    private static final HealBlock instance = new HealBlock();

    @Override
    @NonNull
    public final StatusEffectType getStatusEffectType() {
        return StatusEffectType.HEAL_BLOCK;
    }

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§5§l회복 차단!", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull Damageable combatEntity, long i) {
        if (combatEntity.isCreature() && combatEntity.getEntity() instanceof LivingEntity)
            ((LivingEntity) combatEntity.getEntity()).addPotionEffect(
                    new PotionEffect(PotionEffectType.WITHER, 4, 0, false, false), true);
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    @NonNull
    public final Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.HEALED);
    }
}
