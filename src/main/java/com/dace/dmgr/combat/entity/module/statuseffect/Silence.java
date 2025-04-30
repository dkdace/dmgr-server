package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

/**
 * 침묵 상태 효과를 처리하는 클래스.
 */
public class Silence extends StatusEffect {
    /** 공격자 */
    @Nullable
    private final CombatUser attacker;

    /**
     * 침묵 상태 효과 인스턴스를 생성한다.
     *
     * @param attacker 공격자
     */
    public Silence(@Nullable CombatUser attacker) {
        super(StatusEffectType.SILENCE, false);
        this.attacker = attacker;
    }

    @Override
    @MustBeInvokedByOverriders
    public void onStart(@NonNull Damageable combatEntity) {
        if (combatEntity instanceof CombatUser) {
            ((CombatUser) combatEntity).getUser().sendTitle("§5§l침묵당함!", "", Timespan.ZERO, Timespan.ofTicks(5), Timespan.ofTicks(10));
            ((CombatUser) combatEntity).getActionManager().cancelSkill(attacker);
        }
    }

    @Override
    @MustBeInvokedByOverriders
    public void onTick(@NonNull Damageable combatEntity, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().stopSound("");
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    @NonNull
    public final Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.USE_SKILL, CombatRestriction.HEAR);
    }
}
