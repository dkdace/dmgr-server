package com.dace.dmgr.combat.combatant.palas.action;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.combat.entity.Healable;
import lombok.AccessLevel;
import lombok.NonNull;
import lombok.Setter;

@Setter(AccessLevel.PACKAGE)
public final class PalasP1 extends AbstractSkill {
    /** 현재 사용 대상 */
    private Healable target = null;
    /** 최근 치유량 */
    private double healAmount;

    public PalasP1(@NonNull CombatUser combatUser) {
        super(combatUser, PalasP1Info.getInstance(), Timespan.ZERO, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && target.getDamageModule().isLowHealth();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        target.getDamageModule().heal(combatUser, healAmount, true);
    }

    @Override
    public boolean isCancellable() {
        return false;
    }
}
