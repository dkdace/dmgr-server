package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasDuration;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatUser;

public class ArkaceP1 extends Skill implements HasDuration {
    public ArkaceP1(CombatUser combatUser) {
        super(1, combatUser, ArkaceP1Info.getInstance(), -1);
    }

    @Override
    public long getCooldown() {
        return 0;
    }

    @Override
    public long getDuration() {
        return -1;
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (!isUsing()) {
            use();
            combatUser.addSpeedIncrement(ArkaceP1Info.SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability((short) (combatUser.getWeapon().getActionInfo().getItemStack().getDurability() + 1000));
        } else {
            use();
            combatUser.addSpeedIncrement(-ArkaceP1Info.SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability(combatUser.getWeapon().getActionInfo().getItemStack().getDurability());
        }
    }
}
