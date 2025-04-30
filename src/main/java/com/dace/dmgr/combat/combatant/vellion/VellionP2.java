package com.dace.dmgr.combat.combatant.vellion;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;

@Setter(AccessLevel.PACKAGE)
public final class VellionP2 extends AbstractSkill {
    /** 최근 피해량 */
    private double damageAmount;

    public VellionP2(@NonNull CombatUser combatUser) {
        super(combatUser, VellionP2Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
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
