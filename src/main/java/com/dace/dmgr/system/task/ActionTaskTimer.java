package com.dace.dmgr.system.task;

import com.dace.dmgr.combat.entity.CombatUser;

/**
 * 플레이어가 동작에서 사용하는 타이머.
 *
 * <p>동작 사용 중 죽었을 때 동작을 취소하기 위해 사용한다.</p>
 */
public abstract class ActionTaskTimer extends TaskTimer {
    /** 플레이어 객체 */
    private final CombatUser combatUser;

    protected ActionTaskTimer(CombatUser combatUser, long period, long repeat) {
        super(period, repeat);
        this.combatUser = combatUser;
    }

    protected ActionTaskTimer(CombatUser combatUser, long period) {
        this(combatUser, period, 0);
    }

    @Override
    protected boolean onTimerTick(int i) {
        if (combatUser.isDead())
            return false;
        return onTickAction(i);
    }

    /**
     * @see TaskTimer#onTimerTick(int)
     */
    protected abstract boolean onTickAction(int i);
}
