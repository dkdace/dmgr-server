package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.Damageable;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * 고정 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grounding implements StatusEffect {
    @Getter
    private static final Grounding instance = new Grounding();

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l고정당함!", "", Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity) {
        // 미사용
    }

    @Override
    @NonNull
    public final Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.ACTION_MOVE);
    }
}
