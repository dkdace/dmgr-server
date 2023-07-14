package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.CombatUser;

import java.util.Arrays;
import java.util.List;

public class ArkaceP1 extends Skill {
    public ArkaceP1(CombatUser combatUser) {
        super(1, combatUser, ArkaceP1Info.getInstance(), -1);
    }

    @Override
    public List<ActionKey> getDefaultActionKeys() {
        return Arrays.asList(ActionKey.SPRINT);
    }

    @Override
    public long getDefaultCooldown() {
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (!isUsing()) {
            enable();
            combatUser.addSpeedIncrement(ArkaceP1Info.SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability((short) (combatUser.getWeapon().getActionInfo().getItemStack().getDurability() + 1000));
        } else {
            disable();
            combatUser.addSpeedIncrement(-ArkaceP1Info.SPRINT_SPEED);
            combatUser.getEntity().getEquipment().getItemInMainHand()
                    .setDurability(combatUser.getWeapon().getActionInfo().getItemStack().getDurability());
        }
    }
}
