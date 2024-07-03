package com.dace.dmgr.combat.character.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;

public final class ArkaceP1 extends AbstractSkill {
    /** 수정자 ID */
    private static final String MODIFIER_ID = "ArkaceP1";

    ArkaceP1(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceP1Info.getInstance());
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
        if (isDurationFinished() && !combatUser.getEntity().isSprinting()) {
            setDuration();
            combatUser.getMoveModule().getSpeedStatus().addModifier(MODIFIER_ID, ArkaceP1Info.SPRINT_SPEED);
            combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.SPRINT);
        } else
            onCancelled();
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished() && combatUser.getEntity().isSprinting();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();

        setDuration(0);
        combatUser.getMoveModule().getSpeedStatus().removeModifier(MODIFIER_ID);
        combatUser.getWeapon().displayDurability(ArkaceWeaponInfo.RESOURCE.DEFAULT);
    }
}
