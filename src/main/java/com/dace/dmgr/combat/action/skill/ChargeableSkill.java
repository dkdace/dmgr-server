package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.system.EntityInfoRegistry;
import com.dace.dmgr.system.task.TaskTimer;
import lombok.Getter;

/**
 * 상태 변수를 가지고 있는 충전형 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class ChargeableSkill extends Skill {
    /** 상태 변수 */
    private float stateValue = 0;

    protected ChargeableSkill(int number, CombatUser combatUser, SkillInfo skillInfo, int slot) {
        super(number, combatUser, skillInfo, slot);
    }

    @Override
    protected void onCooldownFinished() {
        super.onCooldownFinished();
        runStateValueCharge();
    }

    @Override
    public final long getDefaultDuration() {
        return -1;
    }

    @Override
    protected void onDurationTick() {
        addStateValue(-(getStateValueDecrement() / 20F));
    }

    /**
     * 스킬의 상태 변수 충전을 실행한다.
     */
    private void runStateValueCharge() {
        new TaskTimer(1) {
            @Override
            public boolean run(int i) {
                if (EntityInfoRegistry.getCombatUser(combatUser.getEntity()) == null)
                    return false;

                addStateValue(getStateValueIncrement() / 20F);

                return (stateValue < getMaxStateValue()) && isDurationFinished() && isCooldownFinished();
            }
        };
    }

    /**
     * 상태 변수의 최댓값을 반환한다.
     *
     * @return 상태 변수의 최댓값
     */
    public abstract int getMaxStateValue();

    /**
     * 지정한 양만큼 스킬의 상태 변수를 증가시킨다.
     *
     * @param increment 증가량
     */
    public final void addStateValue(float increment) {
        stateValue += increment;
        if (stateValue < 0)
            stateValue = 0;
        if (stateValue > getMaxStateValue())
            stateValue = getMaxStateValue();
    }

    /**
     * 상태 변수의 초당 충전량을 반환한다.
     *
     * @return 상태 변수의 초당 충전량
     */
    public abstract int getStateValueIncrement();

    /**
     * 상태 변수의 초당 소모량을 반환한다.
     *
     * @return 상태 변수의 초당 소모량
     */
    public abstract int getStateValueDecrement();
}
