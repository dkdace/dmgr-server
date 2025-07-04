package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class No7P1 extends PassiveSkill {
    public No7P1(@NonNull CombatUser combatUser, @NonNull No7P1Info skillInfo) {
        super(combatUser, skillInfo, No7P1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isCooldownFinished())
            return null;

        return ActionBarDisplay.builder(this).title().cooldownBar().build();
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && combatUser.getAbilityManager().getAbility(No7T1Info.getInstance()).getShield() < No7P1Info.SHIELD;
    }

    @Override
    protected void onUse() {
        setCooldown();

        if (!combatUser.getDamageModule().isLowHealth())
            return;

        No7T1 skillt1 = combatUser.getAbilityManager().getAbility(No7T1Info.getInstance());
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

    /**
     * 매 틱마다 실행할 작업.
     */
    void onTick() {
        if (combatUser.getDamageModule().isLowHealth())
            use(ActionKey.SYSTEM);
    }
}
