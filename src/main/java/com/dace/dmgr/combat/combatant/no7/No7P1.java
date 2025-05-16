package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionBarStringUtil;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class No7P1 extends AbstractSkill {
    public No7P1(@NonNull CombatUser combatUser) {
        super(combatUser, No7P1Info.getInstance(), No7P1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getActionManager().getTrait(No7T1Info.getInstance()).getShield() < No7P1Info.SHIELD;
    }

    @Override
    @Nullable
    public String getActionBarString() {
        return isCooldownFinished() ? null : ActionBarStringUtil.getCooldownBar(this);
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setCooldown();

        if (!combatUser.getDamageModule().isLowHealth())
            return;

        No7T1 skillt1 = combatUser.getActionManager().getTrait(No7T1Info.getInstance());
        skillt1.addShield(No7P1Info.SHIELD - skillt1.getShield());
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished() || !isCooldownFinished();
    }

    @Override
    protected void onCancelled() {
        setCooldown();
    }
}
