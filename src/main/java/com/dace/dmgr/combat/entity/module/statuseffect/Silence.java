package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

import java.util.EnumSet;
import java.util.Set;

/**
 * 침묵 상태 효과를 처리하는 클래스.
 */
public class Silence extends StatusEffect {
    @Getter
    private static final Silence instance = new Silence();

    /**
     * 침묵 상태 효과 인스턴스를 생성한다.
     */
    protected Silence() {
        super(StatusEffectType.SILENCE, false);
    }

    @Override
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        if (combatEntity instanceof CombatUser) {
            ((CombatUser) combatEntity).getUser().sendTitle("§5§l침묵당함!", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));

            if (provider instanceof CombatUser)
                ((CombatUser) combatEntity).cancelSkill((CombatUser) provider);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().stopSound("");
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    @NonNull
    public final Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.USE_SKILL, CombatRestriction.HEAR);
    }
}
