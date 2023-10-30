package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;

/**
 * 고정 상태 효과를 처리하는 클래스.
 */
public class Grounding implements StatusEffect {
    @Override
    public StatusEffectType getStatusEffectType() {
        return StatusEffectType.GROUNDING;
    }

    @Override
    public void onStart(CombatEntity combatEntity) {
    }

    @Override
    public void onTick(CombatEntity combatEntity, int i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().sendTitle("§c§l고정당함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(CombatEntity combatEntity) {
    }
}
