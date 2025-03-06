package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatRestriction;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Damageable;
import lombok.Getter;
import lombok.NonNull;

import java.util.EnumSet;
import java.util.Set;

/**
 * 고정 상태 효과를 처리하는 클래스.
 */
public class Grounding extends StatusEffect {
    @Getter
    private static final Grounding instance = new Grounding();

    /**
     * 고정 상태 효과 인스턴스를 생성한다.
     */
    protected Grounding() {
        super(StatusEffectType.GROUNDING, false);
    }

    @Override
    public void onStart(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull Damageable combatEntity, @NonNull CombatEntity provider, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l고정당함!", "", Timespan.ZERO, Timespan.ofTicks(2), Timespan.ofTicks(10));
    }

    @Override
    public void onEnd(@NonNull Damageable combatEntity, @NonNull CombatEntity provider) {
        // 미사용
    }

    @Override
    @NonNull
    public Set<@NonNull CombatRestriction> getCombatRestrictions(@NonNull Damageable combatEntity) {
        return EnumSet.of(CombatRestriction.ACTION_MOVE);
    }
}
