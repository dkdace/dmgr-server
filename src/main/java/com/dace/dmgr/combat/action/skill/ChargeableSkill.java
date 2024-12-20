package com.dace.dmgr.combat.action.skill;

import com.dace.dmgr.combat.action.info.ActiveSkillInfo;
import com.dace.dmgr.combat.entity.CombatUser;
import com.dace.dmgr.util.task.IntervalTask;
import com.dace.dmgr.util.task.TaskUtil;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.MustBeInvokedByOverriders;

/**
 * 상태 변수를 가지고 있는 충전형 스킬의 상태를 관리하는 클래스.
 */
@Getter
public abstract class ChargeableSkill extends ActiveSkill {
    /** 상태 변수 */
    private int stateValue = 0;

    /**
     * @see ActiveSkill#ActiveSkill(CombatUser, ActiveSkillInfo, int)
     */
    protected ChargeableSkill(@NonNull CombatUser combatUser, @NonNull ActiveSkillInfo<? extends ActiveSkill> activeSkillInfo, int slot) {
        super(combatUser, activeSkillInfo, slot);
    }

    @Override
    protected void onTick() {
        if (isDurationFinished()) {
            if (isCooldownFinished())
                displayReady(1);
            else
                displayCooldown((int) Math.ceil(getCooldown() / 20.0));
        } else {
            displayUsing(1);
            addStateValue(-getStateValueDecrement());
        }
    }

    @Override
    @MustBeInvokedByOverriders
    protected void onCooldownFinished() {
        super.onCooldownFinished();
        runStateValueCharge();
    }

    @Override
    public final long getDefaultDuration() {
        return -1;
    }

    @Override
    @MustBeInvokedByOverriders
    public void reset() {
        super.reset();

        setStateValue(getMaxStateValue());
    }

    /**
     * 스킬의 상태 변수 충전을 실행한다.
     */
    private void runStateValueCharge() {
        TaskUtil.addTask(this, new IntervalTask(i -> {
            addStateValue(getStateValueIncrement());

            return (stateValue < getMaxStateValue()) && isDurationFinished() && isCooldownFinished();
        }, 1));
    }

    /**
     * 상태 변수의 최댓값을 반환한다.
     *
     * @return 상태 변수의 최댓값
     */
    public abstract int getMaxStateValue();

    /**
     * 스킬의 상태 변수를 설정한다.
     *
     * @param stateValue 상태 변수
     */
    public final void setStateValue(int stateValue) {
        this.stateValue = Math.min(Math.max(0, stateValue), getMaxStateValue());
    }

    /**
     * 지정한 양만큼 스킬의 상태 변수를 증가시킨다.
     *
     * @param increment 증가량
     */
    public final void addStateValue(int increment) {
        setStateValue(stateValue + increment);
    }

    /**
     * 상태 변수의 틱당 충전량을 반환한다.
     *
     * @return 상태 변수의 틱당 충전량
     */
    protected abstract int getStateValueIncrement();

    /**
     * 상태 변수의 틱당 소모량을 반환한다.
     *
     * @return 상태 변수의 틱당 소모량
     */
    protected abstract int getStateValueDecrement();
}
