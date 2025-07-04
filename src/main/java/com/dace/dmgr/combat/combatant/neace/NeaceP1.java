package com.dace.dmgr.combat.combatant.neace;

import com.dace.dmgr.Timespan;
import com.dace.dmgr.combat.ability.ActionKey;
import com.dace.dmgr.combat.ability.skill.PassiveSkill;
import com.dace.dmgr.combat.entity.combatuser.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import lombok.NonNull;

import java.util.function.LongConsumer;

public final class NeaceP1 extends PassiveSkill {
    public NeaceP1(@NonNull CombatUser combatUser, @NonNull NeaceP1Info skillInfo) {
        super(combatUser, skillInfo, NeaceP1Info.ACTIVATE_DURATION, Timespan.MAX);
    }

    @Override
    protected boolean canUse() {
        return super.canUse() && isDurationFinished();
    }

    @Override
    protected void onUse() {
        setDuration();

        addActionTask(new IntervalTask((LongConsumer) i ->
                combatUser.getHealModule().heal(combatUser, NeaceP1Info.HEAL_PER_SECOND / 20.0, false), 1));
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
     *
     * @param i 인덱스
     */
    void onTick(long i) {
        if (i % 5 == 0)
            use(ActionKey.SYSTEM);
    }
}
