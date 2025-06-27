package com.dace.dmgr.combat.combatant.no7;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionBarDisplay;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.Set;

public final class No7P1 extends PassiveSkill {
    public No7P1(@NonNull CombatUser combatUser, @NonNull No7P1Info skillInfo) {
        super(combatUser, skillInfo, No7P1Info.COOLDOWN, Timespan.MAX);
    }

    @Override
    @NonNull
    public Set<@NonNull ActionKey> getDefaultActionKeys() {
        return EnumSet.of(ActionKey.PERIODIC_1);
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && combatUser.getAbilityManager().getAbility(No7T1Info.getInstance()).getShield() < No7P1Info.SHIELD;
    }

    @Override
    @Nullable
    public ActionBarDisplay getActionBarDisplay() {
        if (isCooldownFinished())
            return null;

        return ActionBarDisplay.builder(this).title().cooldownBar().build();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
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
            combatUser.getAbilityManager().useAction(ActionKey.PERIODIC_1);
    }
}
