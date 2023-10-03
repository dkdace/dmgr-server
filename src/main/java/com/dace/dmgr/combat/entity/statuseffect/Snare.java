package com.dace.dmgr.combat.entity.statuseffect;

import com.dace.dmgr.combat.entity.CombatEntity;
import com.dace.dmgr.combat.entity.CombatUser;

/**
 * 속박 상태 효과를 처리하는 클래스.
 */
public class Snare implements StatusEffect {
    @Override
    public StatusEffectType getStatusEffectType() {
        return StatusEffectType.SNARE;
    }

    @Override
    public void onStart(CombatEntity<?> combatEntity) {
    }

    @Override
    public void onTick(CombatEntity<?> combatEntity, int i) {
        if (combatEntity instanceof CombatUser)
            ((CombatUser) combatEntity).getEntity().sendTitle("§c§l속박당함!", "", 0, 2, 10);
    }

    @Override
    public void onEnd(CombatEntity<?> combatEntity) {
    }
}
