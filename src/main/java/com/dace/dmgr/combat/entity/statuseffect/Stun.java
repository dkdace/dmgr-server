package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.Getter;

/**
 * 기절 상태 효과를 처리하는 클래스.
 */
public final class Stun implements StatusEffect {
    @Getter
    private static final Stun instance = new Stun();

    @Override
    public void onStart(CombatEntity<?> combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().getInventory().setHeldItemSlot(8);
    }

    @Override
    public void onTick(CombatEntity<?> combatEntity, int i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().sendTitle("§c§l기절함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(CombatEntity<?> combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().getInventory().setHeldItemSlot(4);
    }
}
