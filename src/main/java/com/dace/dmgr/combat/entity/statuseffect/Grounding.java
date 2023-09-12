package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

/**
 * 고정 상태 효과를 처리하는 클래스.
 */
public final class Grounding implements StatusEffect {
    @Getter
    private static final Grounding instance = new Grounding();

    @Override
    public void onStart(CombatEntity<?> combatEntity) {
    }

    @Override
    public void onTick(CombatEntity<?> combatEntity, int i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().sendTitle("§c§l고정당함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(CombatEntity<?> combatEntity) {
    }
}
