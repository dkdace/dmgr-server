package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

/**
 * 침묵 상태 효과를 처리하는 클래스.
 */
public final class Silence implements StatusEffect {
    @Getter
    private static final Silence instance = new Silence();

    @Override
    public void onStart(CombatEntity<?> combatEntity) {
    }

    @Override
    public void onTick(CombatEntity<?> combatEntity, int i) {
        if (combatEntity instanceof CombatUser) {
            ((CombatUser) combatEntity).getEntity().sendTitle("§5§l침묵당함!", "", 0, 2, 10);
            ((CombatUser) combatEntity).getEntity().stopSound("");
        }
    }

    @Override
    public void onEnd(CombatEntity<?> combatEntity) {
    }
}