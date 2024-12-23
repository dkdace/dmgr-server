package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;

public final class NeaceP1 extends AbstractSkill {
    public NeaceP1(@NonNull CombatUser combatUser) {
        super(combatUser);
    }

    @Override
    @NonNull
    public ActionKey @NonNull [] getDefaultActionKeys() {
        return new ActionKey[]{ActionKey.PERIODIC_1};
    }

    @Override
    public long getDefaultCooldown() {
        return NeaceP1Info.ACTIVATE_DURATION;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished();
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            combatUser.getDamageModule().heal(combatUser, NeaceP1Info.HEAL_PER_SECOND / 20.0, false);
            return true;
        }, isCancelled -> onCancelled(), 1));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished() || !isCooldownFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setCooldown();
    }
}
