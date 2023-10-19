package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.Skill;
import com.dace.dmgr.combat.entity.Ability;
import com.dace.dmgr.combat.entity.CombatUser;

public final class ArkaceP1 extends Skill {
    public ArkaceP1(CombatUser combatUser) {
        super(1, combatUser, ArkaceP1Info.getInstance());
    }

    @Override
    public ActionKey[] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.SPRINT};
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
        if (isDurationFinished()) {
            setDuration();
            combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).addModifier("ArkaceP1", ArkaceP1Info.SPRINT_SPEED);
            combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.SPRINT);
        } else {
            setDuration(0);
            combatUser.getAbilityStatusManager().getAbilityStatus(Ability.SPEED).removeModifier("ArkaceP1");
            combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.DEFAULT);
        }
    }
}
