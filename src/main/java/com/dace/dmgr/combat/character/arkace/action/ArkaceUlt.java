package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.HasCost;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.entity.CombatUser;

public class ArkaceUlt extends Skill implements HasCost {
    public ArkaceUlt(CombatUser combatUser) {
        super(4, combatUser, ArkaceUltInfo.getInstance(), 3);
    }

    @Override
    public long getDefaultCooldown() {
        return -1;
    }

    @Override
    public int getCost() {
        return ArkaceUltInfo.COST;
    }

    @Override
    public long getDefaultDuration() {
        return ArkaceUltInfo.DURATION;
    }

    @Override
    public void onUse(ActionKey actionKey) {
        if (!isUsing()) {
            enable();
            ((Reloadable) combatUser.getWeapon()).setRemainingAmmo(ArkaceWeaponInfo.CAPACITY);
        }
    }
}
