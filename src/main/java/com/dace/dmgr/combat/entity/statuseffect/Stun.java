package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.MessageUtil;

/**
 * 기절 상태 효과를 처리하는 클래스.
 */
public class Stun implements StatusEffect {
    @Override
    public StatusEffectType getStatusEffectType() {
        return StatusEffectType.STUN;
    }

    @Override
    public void onStart(CombatEntity combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().getInventory().setHeldItemSlot(8);
    }

    @Override
    public void onTick(CombatEntity combatEntity, int i) {
        if (combatEntity instanceof CombatUser)
            MessageUtil.sendTitle(combatEntity.getEntity(), "§c§l기절함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(CombatEntity combatEntity) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().getInventory().setHeldItemSlot(4);
    }
}
