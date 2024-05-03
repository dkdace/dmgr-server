package com.dace.dmgr.combat.entity.module.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * 고정 상태 효과를 처리하는 클래스.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Grounding implements StatusEffect {
    @Getter
    static final Grounding instance = new Grounding();

    @Override
    public final boolean isPositive() {
        return false;
    }

    @Override
    public void onStart(@NonNull CombatEntity combatEntity) {
        // 미사용
    }

    @Override
    public void onTick(@NonNull CombatEntity combatEntity, long i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getUser().sendTitle("§c§l고정당함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(@NonNull CombatEntity combatEntity) {
        // 미사용
    }
}
