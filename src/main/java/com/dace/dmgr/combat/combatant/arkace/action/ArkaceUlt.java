package com.dace.dmgr.combat.combatant.arkace.action;

import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class ArkaceUlt extends UltimateSkill {
    public ArkaceUlt(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceUltInfo.getInstance(), ArkaceUltInfo.DURATION, ArkaceUltInfo.COST);
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isDurationFinished() ? null : ActionBarStringUtil.getDurationBar(this);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getWeapon().cancel();
        ((ArkaceWeapon) combatUser.getWeapon()).getReloadModule().resetRemainingAmmo();
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
