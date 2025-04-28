package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

import java.util.function.LongConsumer;

public final class NeaceP1 extends AbstractSkill {
    public NeaceP1(@NonNull CombatUser combatUser) {
        super(combatUser, NeaceP1Info.getInstance(), NeaceP1Info.ACTIVATE_DURATION, Timespan.MAX);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        addActionTask(new IntervalTask((LongConsumer) i ->
                combatUser.getDamageModule().heal(combatUser, NeaceP1Info.HEAL_PER_SECOND / 20.0, false), 1));
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
