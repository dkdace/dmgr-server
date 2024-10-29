package com.dace.dmgr.combat.character.neace.action;

import com.dace.dmgr.combat.action.ActionKey;
import com.dace.dmgr.combat.action.skill.AbstractSkill;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.CooldownUtil;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.NonNull;

public final class NeaceP1 extends AbstractSkill {
    /** 쿨타임 ID */
    public static final String COOLDOWN_ID = "NeaceP1";

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
        return 0;
    }

    @Override
    public long getDefaultDuration() {
        return -1;
    }

    @Override
    public boolean canUse(@NonNull ActionKey actionKey) {
        return super.canUse(actionKey) && isDurationFinished() && CooldownUtil.getCooldown(combatUser, COOLDOWN_ID) == 0;
    }

    @Override
    public void onUse(@NonNull ActionKey actionKey) {
        setDuration();

        TaskUtil.addTask(taskRunner, new IntervalTask(i -> {
            if (CooldownUtil.getCooldown(combatUser, COOLDOWN_ID) > 0)
                return false;

            combatUser.getDamageModule().heal(combatUser, NeaceP1Info.HEAL_PER_SECOND / 20, false);
            return true;
        }, isCancelled -> onCancelled(), 1));
    }

    @Override
    public boolean isCancellable() {
        return !isDurationFinished();
    }

    @Override
    public void onCancelled() {
        super.onCancelled();
        setDuration(0);
    }
}
