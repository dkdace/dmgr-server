package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.action.weapon.Reloadable;
import com.dace.dmgr.combat.entity.CombatUser;

public final class ArkaceUlt extends UltimateSkill {
    public ArkaceUlt(CombatUser combatUser) {
        super(4, combatUser, ArkaceUltInfo.getInstance());
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
    public boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    protected void onUseUltimateSkill(ActionKey actionKey) {
        setDuration();
        ((Reloadable) combatUser.getWeapon()).setRemainingAmmo(ArkaceWeaponInfo.CAPACITY);
    }
}
