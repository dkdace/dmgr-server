package com.dace.dmgr.combat.character.vellion.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import lombok.NonNull;
import lombok.Setter;

@Setter
public final class VellionP2 extends AbstractSkill {
    /** 최근 피해량 */
    private double damageAmount;

    public VellionP2(@NonNull CombatUser combatUser) {
        super(combatUser);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
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
        combatUser.getDamageModule().heal(combatUser, damageAmount * VellionP2Info.HEAL_DAMAGE_RATIO, false);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
