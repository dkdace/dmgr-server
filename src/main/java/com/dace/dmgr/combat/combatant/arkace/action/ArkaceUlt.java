package com.dace.dmgr.combat.combatant.arkace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.UltimateSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;

public final class ArkaceUlt extends UltimateSkill {
    public ArkaceUlt(@NonNull CombatUser combatUser) {
        super(combatUser, ArkaceUltInfo.getInstance());
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
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        super.onUse(actionKey);

        setDuration();
        combatUser.getWeapon().onCancelled();
        ((ArkaceWeapon) combatUser.getWeapon()).getReloadModule().setRemainingAmmo(ArkaceWeaponInfo.CAPACITY);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
