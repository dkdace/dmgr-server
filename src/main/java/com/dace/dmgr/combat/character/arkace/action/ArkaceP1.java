package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;

public final class ArkaceP1 extends AbstractSkill {
    public ArkaceP1(@NonNull CombatUser combatUser) {
        super(1, combatUser, ArkaceP1Info.getInstance());
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
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
    public void onUse(@NonNull ActionKey actionKey) {
        if (isDurationFinished()) {
            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier("ArkaceP1", ArkaceP1Info.SPRINT_SPEED);
            combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.SPRINT);
        } else {
            setDuration(0);
            combatUser.getMoveModule().getSpeedStatus().removeModifier("ArkaceP1");
            combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.DEFAULT);
        }
    }
}
